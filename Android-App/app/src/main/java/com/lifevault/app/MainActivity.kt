package com.lifevault.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.lifevault.app.presentation.navigation.LifeVaultNavGraph
import com.lifevault.app.presentation.settings.ThemeViewModel
import com.lifevault.app.presentation.theme.LifeVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeVaultRoot()
        }
    }
}

@Composable
private fun LifeVaultRoot() {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    val pinLockViewModel: com.lifevault.app.presentation.pinlock.PinLockViewModel = hiltViewModel()
    val isPinLockEnabled by pinLockViewModel.isPinLockEnabled.collectAsState()
    var unlocked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    LifeVaultTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (isPinLockEnabled && !unlocked) {
                val storedHash by pinLockViewModel.storedPinHashForUnlock().collectAsState(initial = null)
                storedHash?.let { hash ->
                    com.lifevault.app.presentation.pinlock.PinUnlockScreen(
                        storedPinHash = hash,
                        onUnlocked = { unlocked = true }
                    )
                }
            } else {
                val navController = rememberNavController()
                LifeVaultNavGraph(navController = navController)
            }
        }
    }
}
