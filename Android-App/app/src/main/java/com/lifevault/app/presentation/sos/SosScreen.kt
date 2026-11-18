package com.lifevault.app.presentation.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.feedback.FeedbackDialog
import com.lifevault.app.presentation.theme.ErrorRed
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.SuccessGreen
import java.util.Locale

/**
 * SOS screen.
 *
 * Tapping the big SOS button gets a live location fix and immediately SMSes a Google Maps
 * link to every trusted contact and nominee, then keeps updating their view (Firestore live
 * doc + periodic SMS) until the user taps "I'm Safe". Police / ambulance dial buttons are kept.
 * After stopping, the user is asked for a star rating and review.
 */
@Composable
fun SosScreen(
    onNavigateBack: () -> Unit,
    viewModel: SosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val state by viewModel.state.collectAsState()

    fun dial(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    }

    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locationOk = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val smsOk = result[Manifest.permission.SEND_SMS] == true
        viewModel.start(locationOk, smsOk)
    }

    fun triggerSos() {
        val locationOk = granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            granted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val smsOk = granted(Manifest.permission.SEND_SMS)
        if (locationOk && smsOk) {
            viewModel.start(true, true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.SEND_SMS
                )
            )
        }
    }

    // Keep the screen awake while live sharing so location keeps updating.
    DisposableEffect(state.isActive) {
        view.keepScreenOn = state.isActive
        onDispose { view.keepScreenOn = false }
    }

    // If SMS permission was denied, open the SMS app with the alert pre-filled.
    LaunchedEffect(state.smsDraft) {
        state.smsDraft?.let { draft ->
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + draft.numbers.joinToString(";")))
                intent.putExtra("sms_body", draft.body)
                context.startActivity(intent)
            } catch (e: Exception) {
                // No SMS app available.
            }
            viewModel.consumeSmsDraft()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.verticalGradient(
                        if (state.isActive) listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.7f))
                        else listOf(ErrorRed, ErrorRed.copy(alpha = 0.7f))
                    )
                )
                .clickable(enabled = !state.isActive && !state.isLocating) { triggerSos() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.isLocating) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.height(10.dp))
                    Text("Getting live location...", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                } else {
                    Icon(
                        if (state.isActive) Icons.Filled.MyLocation else Icons.Filled.Sos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.isActive) "SOS ACTIVE - sharing live location" else "TAP TO SEND SOS",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (!state.isActive) {
                        Text(
                            "Alerts your trusted contacts and nominees with your live location",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (state.isActive) {
            Spacer(Modifier.height(16.dp))
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Live sharing is ON", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                    Spacer(Modifier.height(8.dp))
                    val lat = state.latitude
                    val lng = state.longitude
                    Text(
                        if (lat != null && lng != null) {
                            String.format(Locale.US, "Location: %.5f, %.5f", lat, lng)
                        } else {
                            "Waiting for GPS fix..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Alerted ${state.recipientCount} contact(s) - updates sent: ${state.alertsSent}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lat != null && lng != null) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(onClick = {
                            val uri = Uri.parse(String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", lat, lng))
                            try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: Exception) { }
                        }) {
                            Text("View my location on map")
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.stop() },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("I'm Safe - Stop SOS", fontWeight = FontWeight.Bold)
            }
        }

        state.statusMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        SosActionCard(
            icon = Icons.Filled.LocalPolice,
            label = "Call Police",
            subtitle = "100",
            onClick = { dial("100") }
        )
        Spacer(Modifier.height(12.dp))
        SosActionCard(
            icon = Icons.Filled.LocalHospital,
            label = "Call Ambulance",
            subtitle = "108",
            onClick = { dial("108") }
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Tip: add trusted contacts and nominees first - they receive your live location by SMS as soon as you tap SOS.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }

    if (state.justStopped) {
        FeedbackDialog(source = "sos", onDismiss = { viewModel.acknowledgeStopped() })
    }
}

@Composable
private fun SosActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(RoyalPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = RoyalPurple)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
