package com.lifevault.app.presentation.nominee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class NomineeDashboardUiState(
    val nomineeName: String = "",
    val ownerName: String = "",
    val permissions: Set<String> = emptySet(),
    val emergencyProfile: Map<String, Any?>? = null,
    val medicalRecords: Map<String, Any?>? = null,
    val documentTitles: List<String> = emptyList(),
    val sosActive: Boolean = false,
    val sosMapsUrl: String? = null,
    val sosUpdatedAt: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NomineeDashboardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NomineeDashboardUiState())
    val uiState: StateFlow<NomineeDashboardUiState> = _uiState

    private var sosListener: ListenerRegistration? = null

    init { load() }

    /** Live SOS: updates instantly whenever the owner's phone pushes a new location. */
    private fun listenForSos(ownerUid: String) {
        sosListener?.remove()
        sosListener = firestore.collection("users").document(ownerUid)
            .collection("sos").document("active")
            .addSnapshotListener { snapshot, _ ->
                val active = snapshot?.getBoolean("active") == true
                _uiState.update {
                    it.copy(
                        sosActive = active,
                        sosMapsUrl = snapshot?.getString("mapsUrl"),
                        sosUpdatedAt = snapshot?.getLong("updatedAt")
                    )
                }
            }
    }

    override fun onCleared() {
        sosListener?.remove()
        super.onCleared()
    }

    private fun load() {
        val ownerUid = NomineeSession.ownerUid
        val nomineeDocId = NomineeSession.nomineeDocId

        if (ownerUid == null || nomineeDocId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Session expired. Please log in again.") }
            return
        }

        listenForSos(ownerUid)

        viewModelScope.launch {
            try {
                val ownerDoc = firestore.collection("users").document(ownerUid).get().await()
                val ownerName = ownerDoc.getString("fullName") ?: "Unknown"

                val nomineeDoc = firestore.collection("users").document(ownerUid)
                    .collection("nominees").document(nomineeDocId).get().await()
                @Suppress("UNCHECKED_CAST")
                val permissions = (nomineeDoc.get("permissions") as? List<String>)?.toSet() ?: emptySet()

                var emergencyProfile: Map<String, Any?>? = null
                if ("EMERGENCY_PROFILE" in permissions) {
                    val doc = firestore.collection("users").document(ownerUid)
                        .collection("emergencyProfile").document("profile").get().await()
                    if (doc.exists()) emergencyProfile = doc.data
                }

                var medicalRecords: Map<String, Any?>? = null
                if ("MEDICAL_RECORDS" in permissions) {
                    val doc = firestore.collection("users").document(ownerUid)
                        .collection("medicalRecords").document("profile").get().await()
                    if (doc.exists()) medicalRecords = doc.data
                }

                var documentTitles: List<String> = emptyList()
                if ("IMPORTANT_DOCUMENTS" in permissions) {
                    val snapshot = firestore.collection("users").document(ownerUid)
                        .collection("documents").get().await()
                    documentTitles = snapshot.documents.mapNotNull { it.getString("title") }
                }

                _uiState.update {
                    it.copy(
                        nomineeName = NomineeSession.nomineeName ?: "",
                        ownerName = ownerName,
                        permissions = permissions,
                        emergencyProfile = emergencyProfile,
                        medicalRecords = medicalRecords,
                        documentTitles = documentTitles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
            }
        }
    }

    fun logout() {
        NomineeSession.clear()
    }
}
