package com.lifevault.app.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.lifevault.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val isSending: Boolean = false,
    val emailSent: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val errorMessage: String? = null
)

/**
 * Firebase Authentication resets a password through a secure link emailed to the user
 * (the user sets the new password on the Firebase-hosted page). This screen sends that
 * link, shows clear errors, and lets the user resend after a short cooldown.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }

    fun sendResetLink() {
        val state = _uiState.value
        if (state.isSending || state.resendSecondsLeft > 0) return

        val email = state.email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Enter a valid email address") }
            return
        }

        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(isSending = false, emailSent = true, email = email, resendSecondsLeft = RESEND_COOLDOWN_SECONDS)
                    }
                    startCooldown()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSending = false, errorMessage = friendlyMessage(error)) }
                }
            )
        }
    }

    fun onUseDifferentEmail() = _uiState.update {
        it.copy(emailSent = false, errorMessage = null)
    }

    private fun startCooldown() {
        viewModelScope.launch {
            while (_uiState.value.resendSecondsLeft > 0) {
                delay(1_000L)
                _uiState.update { it.copy(resendSecondsLeft = (it.resendSecondsLeft - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "No account found with this email address"
        is FirebaseAuthInvalidCredentialsException -> "That email address is not valid"
        is FirebaseTooManyRequestsException -> "Too many attempts. Please wait a few minutes and try again"
        is FirebaseNetworkException -> "No internet connection. Check your network and try again"
        else -> error.message ?: "Failed to send reset email"
    }

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 30
    }
}
