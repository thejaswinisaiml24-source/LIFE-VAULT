package com.lifevault.app.presentation.securevault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SecureVaultUiState(
    val documentCount: Int = 0,
    val nomineeCount: Int = 0,
    val medicineCount: Int = 0,
    val insuranceCount: Int = 0,
    val legacyItemCount: Int = 0,
    val hasEmergencyProfile: Boolean = false,
    val hasMedicalRecords: Boolean = false,
    val lastSyncEpochMillis: Long? = null,
    val isLoading: Boolean = true
)

private const val USERS_COLLECTION = "users"

/**
 * SCOPE NOTE: the mockup shows "Secure Vault — End-to-End Encrypted" with a
 * storage-used gauge. Real end-to-end encryption and a storage-quota system
 * aren't implemented — this screen instead shows real counts of what's
 * actually stored (documents, nominees, medicines, etc.) pulled live from
 * Firestore. No fake percentage or fake "encrypted" badge.
 */
@HiltViewModel
class SecureVaultViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecureVaultUiState())
    val uiState: StateFlow<SecureVaultUiState> = _uiState

    init { load() }

    private fun load() {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection(USERS_COLLECTION).document(uid)

                val documents = userDoc.collection("documents").get().await().size()
                val nominees = userDoc.collection("nominees").get().await().size()
                val medicines = userDoc.collection("medicines").get().await().size()
                val insurance = userDoc.collection("insurance").get().await().size()

                var legacyCount = 0
                listOf("PERSONAL_MESSAGES", "IMPORTANT_ACCOUNTS", "LETTERS", "WILL_AND_DOCUMENTS").forEach { category ->
                    legacyCount += userDoc.collection("digitalLegacy").document(category)
                        .collection("items").get().await().size()
                }

                val emergencyProfileExists = userDoc.collection("emergencyProfile").document("profile").get().await().exists()
                val medicalRecordsExists = userDoc.collection("medicalRecords").document("profile").get().await().exists()

                val syncDoc = userDoc.collection("meta").document("sync").get().await()
                val lastSync = syncDoc.getLong("lastBackupEpochMillis")

                _uiState.update {
                    it.copy(
                        documentCount = documents,
                        nomineeCount = nominees,
                        medicineCount = medicines,
                        insuranceCount = insurance,
                        legacyItemCount = legacyCount,
                        hasEmergencyProfile = emergencyProfileExists,
                        hasMedicalRecords = medicalRecordsExists,
                        lastSyncEpochMillis = lastSync,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
