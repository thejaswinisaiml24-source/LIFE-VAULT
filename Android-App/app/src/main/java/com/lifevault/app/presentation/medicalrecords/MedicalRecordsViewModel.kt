package com.lifevault.app.presentation.medicalrecords

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

data class MedicalRecordsUiState(
    val doctorName: String = "",
    val hospitalName: String = "",
    val chronicConditions: String = "",
    val currentMedications: String = "",
    val vaccinationHistory: String = "",
    val medicalHistoryNotes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSucceeded: Boolean = false,
    val isEditing: Boolean = false
)

private const val USERS_COLLECTION = "users"
private const val DOC_ID = "profile"
private const val SUBCOLLECTION = "medicalRecords"

@HiltViewModel
class MedicalRecordsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalRecordsUiState())
    val uiState: StateFlow<MedicalRecordsUiState> = _uiState

    init { load() }

    private fun docRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        firestore.collection(USERS_COLLECTION).document(uid).collection(SUBCOLLECTION).document(DOC_ID)
    }

    private fun load() {
        val ref = docRef() ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val snapshot = ref.get().await()
                if (snapshot.exists()) {
                    _uiState.update {
                        it.copy(
                            doctorName = snapshot.getString("doctorName") ?: "",
                            hospitalName = snapshot.getString("hospitalName") ?: "",
                            chronicConditions = snapshot.getString("chronicConditions") ?: "",
                            currentMedications = snapshot.getString("currentMedications") ?: "",
                            vaccinationHistory = snapshot.getString("vaccinationHistory") ?: "",
                            medicalHistoryNotes = snapshot.getString("medicalHistoryNotes") ?: "",
                            isLoading = false,
                            isEditing = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isEditing = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isEditing = true) }
            }
        }
    }

    fun onDoctorNameChange(v: String) = _uiState.update { it.copy(doctorName = v) }
    fun onHospitalNameChange(v: String) = _uiState.update { it.copy(hospitalName = v) }
    fun onChronicConditionsChange(v: String) = _uiState.update { it.copy(chronicConditions = v) }
    fun onCurrentMedicationsChange(v: String) = _uiState.update { it.copy(currentMedications = v) }
    fun onVaccinationHistoryChange(v: String) = _uiState.update { it.copy(vaccinationHistory = v) }
    fun onMedicalHistoryNotesChange(v: String) = _uiState.update { it.copy(medicalHistoryNotes = v) }
    fun onEnableEdit() = _uiState.update { it.copy(isEditing = true, saveSucceeded = false) }
    fun onCancelEdit() = _uiState.update { it.copy(isEditing = false, saveError = null) }
    fun consumeSaveSuccess() = _uiState.update { it.copy(saveSucceeded = false) }

    fun save() {
        val ref = docRef() ?: run {
            _uiState.update { it.copy(saveError = "You must be signed in") }
            return
        }
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "doctorName" to state.doctorName.trim(),
                    "hospitalName" to state.hospitalName.trim(),
                    "chronicConditions" to state.chronicConditions.trim(),
                    "currentMedications" to state.currentMedications.trim(),
                    "vaccinationHistory" to state.vaccinationHistory.trim(),
                    "medicalHistoryNotes" to state.medicalHistoryNotes.trim(),
                    "updatedAtEpochMillis" to System.currentTimeMillis()
                )
                ref.set(data).await()

                firebaseAuth.currentUser?.uid?.let { uid ->
                    firestore.collection(USERS_COLLECTION).document(uid).collection("activities")
                        .add(
                            hashMapOf(
                                "description" to "Medical Records updated",
                                "timestamp" to System.currentTimeMillis()
                            )
                        ).await()
                }

                _uiState.update { it.copy(isSaving = false, saveSucceeded = true, isEditing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save") }
            }
        }
    }
}
