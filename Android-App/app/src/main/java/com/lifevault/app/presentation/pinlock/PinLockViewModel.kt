package com.lifevault.app.presentation.pinlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.data.preferences.AppPreferences
import com.lifevault.app.data.util.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinSetupUiState(
    val pin: String = "",
    val confirmPin: String = "",
    val error: String? = null,
    val saveSucceeded: Boolean = false
)

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val isPinLockEnabled: StateFlow<Boolean> = appPreferences.isPinLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun storedPinHashForUnlock() = appPreferences.pinHash

    private val _uiState = MutableStateFlow(PinSetupUiState())
    val uiState: StateFlow<PinSetupUiState> = _uiState

    fun onPinChange(v: String) {
        if (v.length <= 4 && v.all { it.isDigit() }) _uiState.update { it.copy(pin = v, error = null) }
    }
    fun onConfirmPinChange(v: String) {
        if (v.length <= 4 && v.all { it.isDigit() }) _uiState.update { it.copy(confirmPin = v, error = null) }
    }

    fun savePin() {
        val state = _uiState.value
        if (state.pin.length != 4) {
            _uiState.update { it.copy(error = "PIN must be exactly 4 digits") }
            return
        }
        if (state.pin != state.confirmPin) {
            _uiState.update { it.copy(error = "PINs do not match") }
            return
        }
        viewModelScope.launch {
            appPreferences.setPin(PasswordHasher.hash(state.pin))
            _uiState.update { it.copy(saveSucceeded = true) }
        }
    }

    fun disablePinLock() {
        viewModelScope.launch { appPreferences.disablePinLock() }
    }
}
