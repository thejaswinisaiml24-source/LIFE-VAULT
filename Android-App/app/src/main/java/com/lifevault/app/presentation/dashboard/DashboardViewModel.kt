package com.lifevault.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifevault.app.domain.model.LifeVaultUser
import com.lifevault.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class RecentActivity(
    val id: String,
    val description: String,
    val timestampEpochMillis: Long
)

data class UpcomingMedicine(
    val id: String,
    val name: String,
    val dose: String,
    val time: String
)

data class MedicalSummary(
    val bloodGroup: String,
    val age: String,
    val gender: String,
    val allergies: String
)

data class ExpiryAlert(
    val label: String,
    val expiryText: String,
    val daysRemaining: Long
)

data class DashboardUiState(
    val user: LifeVaultUser? = null,
    val nomineeCount: Int = 0,
    val documentCount: Int = 0,
    val insuranceCount: Int = 0,
    val lastBackupEpochMillis: Long? = null,
    val recentActivities: List<RecentActivity> = emptyList(),
    val upcomingMedicines: List<UpcomingMedicine> = emptyList(),
    val medicalSummary: MedicalSummary? = null,
    val expiryAlerts: List<ExpiryAlert> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            authRepository.currentUser().collect { user ->
                _uiState.update { it.copy(user = user, isLoading = user == null) }
                if (user != null) loadDashboardData(user.uid)
            }
        }
    }

    private suspend fun loadDashboardData(uid: String) {
        try {
            val userDoc = firestore.collection("users").document(uid)

            val nomineesSnapshot = userDoc.collection("nominees").get().await()
            val documentsSnapshot = userDoc.collection("documents").get().await()
            val insuranceSnapshot = userDoc.collection("insurance").get().await()

            val activitySnapshot = userDoc.collection("activities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val activities = activitySnapshot.documents.mapNotNull { doc ->
                val description = doc.getString("description") ?: return@mapNotNull null
                val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                RecentActivity(id = doc.id, description = description, timestampEpochMillis = timestamp)
            }

            val medicinesSnapshot = userDoc.collection("medicines")
                .orderBy("createdAtEpochMillis", Query.Direction.DESCENDING)
                .get()
                .await()
            val upcomingMedicines = medicinesSnapshot.documents
                .filter { it.getBoolean("taken") != true }
                .mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    UpcomingMedicine(
                        id = doc.id,
                        name = name,
                        dose = doc.getString("dose") ?: "",
                        time = doc.getString("time") ?: ""
                    )
                }
                .take(4)

            val emergencyProfileDoc = userDoc.collection("emergencyProfile").document("profile").get().await()
            val medicalSummary = if (emergencyProfileDoc.exists()) {
                MedicalSummary(
                    bloodGroup = emergencyProfileDoc.getString("bloodGroup") ?: "",
                    age = emergencyProfileDoc.getString("age") ?: "",
                    gender = emergencyProfileDoc.getString("gender") ?: "",
                    allergies = emergencyProfileDoc.getString("allergies") ?: ""
                )
            } else null

            val syncDoc = userDoc.collection("meta").document("sync").get().await()
            val lastBackup = syncDoc.getLong("lastBackupEpochMillis")

            // Best-effort expiry parsing: validTill/expiryDate are free-text
            // fields the user typed, so only entries matching a recognized
            // date format produce an alert — unparseable text is silently
            // skipped rather than guessed at.
            val expiryAlerts = mutableListOf<ExpiryAlert>()
            insuranceSnapshot.documents.forEach { doc ->
                val validTill = doc.getString("validTill")
                val type = doc.getString("type") ?: "Insurance"
                parseLenientDate(validTill)?.let { expiryMillis ->
                    val days = daysUntil(expiryMillis)
                    if (days <= 30) {
                        expiryAlerts.add(ExpiryAlert("$type policy", validTill ?: "", days))
                    }
                }
            }
            documentsSnapshot.documents.forEach { doc ->
                val expiryDate = doc.getString("expiryDate")
                val title = doc.getString("title") ?: "Document"
                parseLenientDate(expiryDate)?.let { expiryMillis ->
                    val days = daysUntil(expiryMillis)
                    if (days <= 30) {
                        expiryAlerts.add(ExpiryAlert(title, expiryDate ?: "", days))
                    }
                }
            }

            _uiState.update {
                it.copy(
                    nomineeCount = nomineesSnapshot.size(),
                    documentCount = documentsSnapshot.size(),
                    insuranceCount = insuranceSnapshot.size(),
                    recentActivities = activities,
                    upcomingMedicines = upcomingMedicines,
                    medicalSummary = medicalSummary,
                    expiryAlerts = expiryAlerts.sortedBy { alert -> alert.daysRemaining },
                    lastBackupEpochMillis = lastBackup,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun parseLenientDate(text: String?): Long? {
        if (text.isNullOrBlank()) return null
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "MMM d yyyy", "MMM d, yyyy", "d MMM yyyy")
        for (pattern in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                sdf.isLenient = false
                return sdf.parse(text.trim())?.time
            } catch (e: Exception) {
                // try next format
            }
        }
        return null
    }

    private fun daysUntil(epochMillis: Long): Long {
        val diff = epochMillis - System.currentTimeMillis()
        return diff / (1000 * 60 * 60 * 24)
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}
