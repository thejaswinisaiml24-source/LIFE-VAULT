package com.lifevault.app.presentation.editprofile

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

data class EditProfileUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    init { load() }

    private fun load() {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                _uiState.update {
                    it.copy(
                        fullName = doc.getString("fullName") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        email = doc.getString("email") ?: "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, error = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phoneNumber = v, error = null) }

    fun save() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid)
                    .update(
                        mapOf(
                            "fullName" to state.fullName.trim(),
                            "phoneNumber" to state.phoneNumber.trim()
                        )
                    ).await()
                _uiState.update { it.copy(isSaving = false, saveSucceeded = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun consumeSaveSuccess() = _uiState.update { it.copy(saveSucceeded = false) }
}
