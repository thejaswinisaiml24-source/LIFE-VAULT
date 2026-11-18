package com.lifevault.app.presentation.nominee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lifevault.app.data.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class NomineeLoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false
)

/**
 * Nominee session info held after a successful nominee login — kept simple
 * (in-memory, passed via navigation) rather than a full session/token
 * system, since that's out of scope for this pass.
 */
object NomineeSession {
    var ownerUid: String? = null
    var nomineeDocId: String? = null
    var nomineeName: String? = null

    fun clear() {
        ownerUid = null
        nomineeDocId = null
        nomineeName = null
    }
}

@HiltViewModel
class NomineeLoginViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NomineeLoginUiState())
    val uiState: StateFlow<NomineeLoginUiState> = _uiState

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter email and password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val emailKey = state.email.trim().lowercase()
                val doc = firestore.collection("nominee_credentials").document(emailKey).get().await()

                if (!doc.exists()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No nominee account found for this email") }
                    return@launch
                }

                val storedHash = doc.getString("passwordHash")
                val enteredHash = PasswordHasher.hash(state.password)

                if (storedHash != enteredHash) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Incorrect password") }
                    return@launch
                }

                NomineeSession.ownerUid = doc.getString("ownerUid")
                NomineeSession.nomineeDocId = doc.getString("nomineeDocId")
                NomineeSession.nomineeName = doc.getString("fullName")

                _uiState.update { it.copy(isLoading = false, loginSucceeded = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Login failed") }
            }
        }
    }
}
