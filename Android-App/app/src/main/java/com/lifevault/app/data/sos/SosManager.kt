package com.lifevault.app.data.sos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Pre-filled SMS the screen can open in the user's SMS app when SEND_SMS permission was denied. */
data class SmsDraft(val numbers: List<String>, val body: String)

data class SosState(
    val isLocating: Boolean = false,
    val isActive: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val recipientCount: Int = 0,
    val alertsSent: Int = 0,
    val statusMessage: String? = null,
    val smsDraft: SmsDraft? = null,
    val justStopped: Boolean = false
)

/**
 * App-wide SOS engine (singleton, so tracking keeps running when the user leaves the SOS screen
 * but the app is still alive).
 *
 * On start():
 *  1. loads trusted contacts + nominees phone numbers from Firestore,
 *  2. gets a live GPS/network location fix (waits up to 8 seconds for a fresh one),
 *  3. immediately SMSes a Google Maps link to every contact,
 *  4. writes live location to users/{uid}/sos/active every 10 seconds (nominees see it in their dashboard),
 *  5. re-sends the SMS with the latest location every 2 minutes until the user taps "I'm Safe".
 */
@Singleton
class SosManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SosState())
    val state: StateFlow<SosState> = _state.asStateFlow()

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val latest = MutableStateFlow<Location?>(null)
    private var loopJob: Job? = null
    private var recipients: List<String> = emptyList()
    private var smsAllowed = false
    private var userName = "A LifeVault user"

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latest.value = location
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun start(locationGranted: Boolean, smsGranted: Boolean) {
        val current = _state.value
        if (current.isActive || current.isLocating) return

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            _state.update { it.copy(statusMessage = "Please sign in to use SOS alerts.") }
            return
        }

        smsAllowed = smsGranted && hasPermission(Manifest.permission.SEND_SMS)
        latest.value = null
        _state.value = SosState(isLocating = true, statusMessage = "Getting your live location...")

        loopJob = scope.launch {
            recipients = try { loadRecipients(uid) } catch (e: Exception) { emptyList() }
            userName = try { loadUserName(uid) } catch (e: Exception) { "A LifeVault user" }

            val hasProvider = locationGranted && startLocationUpdates()
            val lastKnown = if (hasProvider) bestLastKnown() else null
            val fresh = if (hasProvider) {
                withTimeoutOrNull(8_000L) { latest.filterNotNull().first() }
            } else null
            val first = fresh ?: lastKnown
            if (latest.value == null && first != null) latest.value = first

            val startedAt = System.currentTimeMillis()
            _state.update {
                it.copy(
                    isLocating = false,
                    isActive = true,
                    recipientCount = recipients.size,
                    latitude = first?.latitude,
                    longitude = first?.longitude,
                    statusMessage = if (first == null) {
                        "Location unavailable - turn on GPS. Alert sent without location."
                    } else null
                )
            }

            // Keep the on-screen coordinates in sync with new fixes.
            launch {
                latest.filterNotNull().collect { loc ->
                    _state.update { it.copy(latitude = loc.latitude, longitude = loc.longitude) }
                }
            }

            pushLive(uid, first, true, startedAt)
            logActivity(uid)
            deliverAlert(first, initial = true)

            // If we had no fix at all, alert again the moment the first fix arrives.
            if (first == null && hasProvider) {
                launch {
                    val fix = latest.filterNotNull().first()
                    deliverAlert(fix, initial = false)
                }
            }

            var tick = 0
            while (isActive) {
                delay(10_000L)
                tick++
                val loc = latest.value
                pushLive(uid, loc, true, startedAt)
                if (tick % 12 == 0) deliverAlert(loc, initial = false)
            }
        }
    }

    fun stop() {
        val uid = firebaseAuth.currentUser?.uid
        val loc = latest.value
        val wasActive = _state.value.isActive

        loopJob?.cancel()
        loopJob = null
        try { locationManager.removeUpdates(listener) } catch (e: Exception) { }

        _state.value = SosState(justStopped = wasActive)

        if (uid != null && wasActive) {
            scope.launch {
                pushLive(uid, loc, false, null)
                if (smsAllowed && recipients.isNotEmpty()) {
                    sendSms("$userName is now safe. SOS alert has been stopped. - LifeVault")
                }
            }
        }
    }

    fun consumeSmsDraft() = _state.update { it.copy(smsDraft = null) }
    fun acknowledgeStopped() = _state.update { it.copy(justStopped = false) }

    // ---------- internals ----------

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(): Boolean {
        var started = false
        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(provider, 5_000L, 5f, listener, Looper.getMainLooper())
                    started = true
                }
            } catch (e: SecurityException) {
                // Provider not allowed with the permission level granted (e.g. GPS with coarse only).
            } catch (e: Exception) {
            }
        }
        return started
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnown(): Location? {
        var best: Location? = null
        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            try {
                val loc = locationManager.getLastKnownLocation(provider)
                if (loc != null && (best == null || loc.time > best.time)) best = loc
            } catch (e: Exception) {
            }
        }
        return best
    }

    private suspend fun loadRecipients(uid: String): List<String> {
        val userDoc = firestore.collection("users").document(uid)
        val numbers = mutableListOf<String>()

        val contacts = userDoc.collection("trustedContacts").get().await()
        contacts.documents.forEach { doc -> doc.getString("phone")?.let { numbers.add(it) } }

        val nominees = userDoc.collection("nominees").get().await()
        nominees.documents.forEach { doc -> doc.getString("phoneNumber")?.let { numbers.add(it) } }

        return numbers.mapNotNull { normalizePhone(it) }.distinct()
    }

    private suspend fun loadUserName(uid: String): String {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.getString("fullName")?.takeIf { it.isNotBlank() } ?: "A LifeVault user"
    }

    private fun normalizePhone(raw: String): String? {
        val cleaned = raw.filter { it.isDigit() || it == '+' }
        if (cleaned.length < 7) return null
        // Plain 10-digit Indian mobile numbers get the +91 country code.
        return if (cleaned.length == 10 && !cleaned.startsWith("+")) "+91$cleaned" else cleaned
    }

    private fun mapsUrl(loc: Location) =
        String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", loc.latitude, loc.longitude)

    private fun alertBody(loc: Location?): String =
        if (loc != null) {
            "SOS ALERT! $userName needs help. Live location: ${mapsUrl(loc)} - LifeVault"
        } else {
            "SOS ALERT! $userName needs help. Location is not available yet, please call immediately. - LifeVault"
        }

    private fun deliverAlert(loc: Location?, initial: Boolean) {
        if (recipients.isEmpty()) {
            _state.update {
                it.copy(statusMessage = "No trusted contacts or nominees found. Add them so they can be alerted.")
            }
            return
        }
        val body = alertBody(loc)
        if (smsAllowed) {
            val sent = sendSms(body)
            _state.update {
                it.copy(
                    alertsSent = it.alertsSent + 1,
                    statusMessage = if (sent > 0) null else "Could not send SMS. Check your SIM/network."
                )
            }
        } else if (initial) {
            _state.update {
                it.copy(
                    smsDraft = SmsDraft(recipients, body),
                    statusMessage = "SMS permission denied - send the prepared message manually."
                )
            }
        }
    }

    private fun smsManager(): SmsManager? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    } catch (e: Exception) {
        null
    }

    private fun sendSms(body: String): Int {
        val sm = smsManager() ?: return 0
        var sent = 0
        val parts = try { sm.divideMessage(body) } catch (e: Exception) { return 0 }
        for (number in recipients) {
            try {
                if (parts.size > 1) {
                    sm.sendMultipartTextMessage(number, null, parts, null, null)
                } else {
                    sm.sendTextMessage(number, null, body, null, null)
                }
                sent++
            } catch (e: Exception) {
            }
        }
        return sent
    }

    private suspend fun pushLive(uid: String, loc: Location?, active: Boolean, startedAt: Long?) {
        try {
            val data = hashMapOf<String, Any>(
                "active" to active,
                "updatedAt" to System.currentTimeMillis()
            )
            if (startedAt != null) data["startedAt"] = startedAt
            if (loc != null) {
                data["latitude"] = loc.latitude
                data["longitude"] = loc.longitude
                data["mapsUrl"] = mapsUrl(loc)
            }
            firestore.collection("users").document(uid)
                .collection("sos").document("active")
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            // Live sync is best-effort; the SMS path does not depend on it.
        }
    }

    private suspend fun logActivity(uid: String) {
        try {
            firestore.collection("users").document(uid).collection("activities")
                .add(
                    hashMapOf(
                        "description" to "SOS alert triggered",
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
        }
    }
}
