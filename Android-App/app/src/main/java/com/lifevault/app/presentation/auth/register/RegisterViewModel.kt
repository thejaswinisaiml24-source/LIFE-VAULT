package com.lifevault.app.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val agreedToPrivacyPolicy: Boolean = false,
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val submissionError: String? = null,
    val registrationSucceeded: Boolean = false
) {
    val isFormValid: Boolean
        get() = fullName.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            phoneNumber.length >= 10 &&
            password.length >= 8 &&
            password == confirmPassword &&
            agreedToPrivacyPolicy
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, submissionError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, submissionError = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phoneNumber = value, submissionError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, submissionError = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, submissionError = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onToggleConfirmPasswordVisibility() = _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    fun onTogglePrivacyPolicy() = _uiState.update { it.copy(agreedToPrivacyPolicy = !it.agreedToPrivacyPolicy) }

    fun onGoogleSignInResult(idToken: String?) {
        if (idToken == null) {
            _uiState.update { it.copy(submissionError = "Google sign-in was cancelled or failed") }
            return
        }
        _uiState.update { it.copy(isLoading = true, submissionError = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, registrationSucceeded = true) } },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, submissionError = error.message ?: "Google sign-in failed")
                    }
                }
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()

        if (state.fullName.isBlank()) errors["fullName"] = "Full name is required"
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) errors["email"] = "Enter a valid email"
        if (state.phoneNumber.length < 10) errors["phone"] = "Enter a valid phone number"
        if (state.password.length < 8) errors["password"] = "Password must be at least 8 characters"
        if (state.password != state.confirmPassword) errors["confirmPassword"] = "Passwords do not match"
        if (!state.agreedToPrivacyPolicy) errors["privacy"] = "You must agree to the Privacy Policy"

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors) }
            return
        }

        _uiState.update { it.copy(isLoading = true, fieldErrors = emptyMap(), submissionError = null) }

        viewModelScope.launch {
            val result = authRepository.register(
                fullName = state.fullName.trim(),
                email = state.email.trim(),
                phoneNumber = state.phoneNumber.trim(),
                password = state.password
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, registrationSucceeded = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, submissionError = error.message ?: "Registration failed")
                    }
                }
            )
        }
    }
}
