package com.lifevault.app.presentation.nominee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lifevault.app.data.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class NomineePermission(val label: String) {
    EMERGENCY_PROFILE("Emergency Profile"),
    MEDICAL_RECORDS("Medical Records"),
    IMPORTANT_DOCUMENTS("Important Documents"),
    DIGITAL_LEGACY("Digital Legacy"),
    PASSWORD_VAULT("Password Vault")
}

data class AddNomineeUiState(
    val fullName: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val governmentId: String = "",
    val loginPassword: String = "",
    val permissions: Map<NomineePermission, Boolean> = NomineePermission.entries.associateWith { false },
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saveSucceeded: Boolean = false,
    val saveError: String? = null
) {
    val isFormValid: Boolean
        get() = fullName.isNotBlank() &&
            relationship.isNotBlank() &&
            phoneNumber.length >= 10 &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            governmentId.isNotBlank() &&
            loginPassword.length >= 6
}

/**
 * Nominees are NOT Firebase Auth users — they log in against a separate
 * top-level "nominee_credentials" collection keyed by their email, which
 * stores a SHA-256 password hash plus a pointer back to (ownerUid,
 * nomineeDocId). This lets a nominee sign in with just email+password
 * without needing their own Firebase Auth account.
 */
@HiltViewModel
class AddNomineeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNomineeUiState())
    val uiState: StateFlow<AddNomineeUiState> = _uiState

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value) }
    fun onRelationshipChange(value: String) = _uiState.update { it.copy(relationship = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phoneNumber = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onGovernmentIdChange(value: String) = _uiState.update { it.copy(governmentId = value) }
    fun onLoginPasswordChange(value: String) = _uiState.update { it.copy(loginPassword = value) }

    fun onTogglePermission(permission: NomineePermission) {
        _uiState.update { state ->
            val updated = state.permissions.toMutableMap()
            updated[permission] = !(updated[permission] ?: false)
            state.copy(permissions = updated)
        }
    }

    fun save() {
        val state = _uiState.value
        val uid = firebaseAuth.currentUser?.uid

        if (uid == null) {
            _uiState.update { it.copy(saveError = "You must be signed in to add a nominee") }
            return
        }

        val errors = mutableMapOf<String, String>()
        if (state.fullName.isBlank()) errors["fullName"] = "Nominee name is required"
        if (state.relationship.isBlank()) errors["relationship"] = "Relationship is required"
        if (state.phoneNumber.length < 10) errors["phone"] = "Enter a valid phone number"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) errors["email"] = "Enter a valid email"
        if (state.governmentId.isBlank()) errors["governmentId"] = "Government ID is required"
        if (state.loginPassword.length < 6) errors["loginPassword"] = "Password must be at least 6 characters"

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true, fieldErrors = emptyMap(), saveError = null) }

        viewModelScope.launch {
            try {
                val emailKey = state.email.trim().lowercase()
                val credentialRef = firestore.collection("nominee_credentials").document(emailKey)

                // Refuse to overwrite an existing nominee login (email already used elsewhere).
                val existingCredential = credentialRef.get().await()
                if (existingCredential.exists()) {
                    _uiState.update {
                        it.copy(isSaving = false, saveError = "This email is already registered as a nominee login")
                    }
                    return@launch
                }

                val nomineeData = hashMapOf(
                    "fullName" to state.fullName.trim(),
                    "relationship" to state.relationship.trim(),
                    "phoneNumber" to state.phoneNumber.trim(),
                    "email" to state.email.trim(),
                    "address" to state.address.trim(),
                    "governmentId" to state.governmentId.trim(),
                    "permissions" to state.permissions.filterValues { it }.keys.map { it.name },
                    "createdAtEpochMillis" to System.currentTimeMillis()
                )

                val nomineeDocRef = firestore.collection("users")
                    .document(uid)
                    .collection("nominees")
                    .add(nomineeData)
                    .await()

                credentialRef.set(
                    hashMapOf(
                        "ownerUid" to uid,
                        "nomineeDocId" to nomineeDocRef.id,
                        "passwordHash" to PasswordHasher.hash(state.loginPassword),
                        "fullName" to state.fullName.trim()
                    )
                ).await()

                firestore.collection("users")
                    .document(uid)
                    .collection("activities")
                    .add(
                        hashMapOf(
                            "description" to "Nominee added: ${state.fullName.trim()}",
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                    .await()

                _uiState.update { it.copy(isSaving = false, saveSucceeded = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = e.message ?: "Failed to save nominee")
                }
            }
        }
    }
}
