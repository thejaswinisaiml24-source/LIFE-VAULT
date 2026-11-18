package com.lifevault.app.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val emailOrPhone: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSucceeded: Boolean = false
) {
    val isFormValid: Boolean
        get() = emailOrPhone.isNotBlank() && password.isNotBlank()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailOrPhoneChange(value: String) = _uiState.update { it.copy(emailOrPhone = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onGoogleSignInResult(idToken: String?) {
        if (idToken == null) {
            _uiState.update { it.copy(errorMessage = "Google sign-in was cancelled or failed") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, loginSucceeded = true) } },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Google sign-in failed")
                    }
                }
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.isFormValid) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.login(state.emailOrPhone.trim(), state.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, loginSucceeded = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Login failed")
                    }
                }
            )
        }
    }
}
