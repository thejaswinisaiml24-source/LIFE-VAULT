package com.lifevault.app.presentation.emergencyprofile

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

data class EmergencyProfileUiState(
    val bloodGroup: String = "",
    val age: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val organDonor: Boolean = false,
    val allergies: String = "",
    val medicalConditions: String = "",
    val currentMedicines: String = "",
    val emergencyNotes: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSucceeded: Boolean = false,
    val isEditing: Boolean = false
)

private const val USERS_COLLECTION = "users"
private const val DOC_ID = "profile"
private const val SUBCOLLECTION = "emergencyProfile"

@HiltViewModel
class EmergencyProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyProfileUiState())
    val uiState: StateFlow<EmergencyProfileUiState> = _uiState

    init {
        load()
    }

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
                            bloodGroup = snapshot.getString("bloodGroup") ?: "",
                            age = snapshot.getString("age") ?: "",
                            gender = snapshot.getString("gender") ?: "",
                            height = snapshot.getString("height") ?: "",
                            weight = snapshot.getString("weight") ?: "",
                            organDonor = snapshot.getBoolean("organDonor") ?: false,
                            allergies = snapshot.getString("allergies") ?: "",
                            medicalConditions = snapshot.getString("medicalConditions") ?: "",
                            currentMedicines = snapshot.getString("currentMedicines") ?: "",
                            emergencyNotes = snapshot.getString("emergencyNotes") ?: "",
                            emergencyContactName = snapshot.getString("emergencyContactName") ?: "",
                            emergencyContactPhone = snapshot.getString("emergencyContactPhone") ?: "",
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

    fun onBloodGroupChange(v: String) = _uiState.update { it.copy(bloodGroup = v) }
    fun onAgeChange(v: String) = _uiState.update { it.copy(age = v) }
    fun onGenderChange(v: String) = _uiState.update { it.copy(gender = v) }
    fun onHeightChange(v: String) = _uiState.update { it.copy(height = v) }
    fun onWeightChange(v: String) = _uiState.update { it.copy(weight = v) }
    fun onToggleOrganDonor() = _uiState.update { it.copy(organDonor = !it.organDonor) }
    fun onAllergiesChange(v: String) = _uiState.update { it.copy(allergies = v) }
    fun onMedicalConditionsChange(v: String) = _uiState.update { it.copy(medicalConditions = v) }
    fun onCurrentMedicinesChange(v: String) = _uiState.update { it.copy(currentMedicines = v) }
    fun onEmergencyNotesChange(v: String) = _uiState.update { it.copy(emergencyNotes = v) }
    fun onEmergencyContactNameChange(v: String) = _uiState.update { it.copy(emergencyContactName = v) }
    fun onEmergencyContactPhoneChange(v: String) = _uiState.update { it.copy(emergencyContactPhone = v) }
    fun onEnableEdit() = _uiState.update { it.copy(isEditing = true, saveSucceeded = false) }
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
                    "bloodGroup" to state.bloodGroup.trim(),
                    "age" to state.age.trim(),
                    "gender" to state.gender.trim(),
                    "height" to state.height.trim(),
                    "weight" to state.weight.trim(),
                    "organDonor" to state.organDonor,
                    "allergies" to state.allergies.trim(),
                    "medicalConditions" to state.medicalConditions.trim(),
                    "currentMedicines" to state.currentMedicines.trim(),
                    "emergencyNotes" to state.emergencyNotes.trim(),
                    "emergencyContactName" to state.emergencyContactName.trim(),
                    "emergencyContactPhone" to state.emergencyContactPhone.trim(),
                    "updatedAtEpochMillis" to System.currentTimeMillis()
                )
                ref.set(data).await()

                firebaseAuth.currentUser?.uid?.let { uid ->
                    firestore.collection(USERS_COLLECTION).document(uid).collection("activities")
                        .add(
                            hashMapOf(
                                "description" to "Emergency Profile updated",
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
