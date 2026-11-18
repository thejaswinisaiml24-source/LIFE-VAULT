package com.lifevault.app.presentation.sos

import androidx.lifecycle.ViewModel
import com.lifevault.app.data.sos.SosManager
import com.lifevault.app.data.sos.SosState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    private val manager: SosManager
) : ViewModel() {
    val state: StateFlow<SosState> = manager.state

    fun start(locationGranted: Boolean, smsGranted: Boolean) = manager.start(locationGranted, smsGranted)
    fun stop() = manager.stop()
    fun consumeSmsDraft() = manager.consumeSmsDraft()
    fun acknowledgeStopped() = manager.acknowledgeStopped()
}
