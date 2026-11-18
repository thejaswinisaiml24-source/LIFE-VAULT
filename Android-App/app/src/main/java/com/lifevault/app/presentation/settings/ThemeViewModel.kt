package com.lifevault.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = appPreferences.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDarkTheme(enabled) }
    }
}
