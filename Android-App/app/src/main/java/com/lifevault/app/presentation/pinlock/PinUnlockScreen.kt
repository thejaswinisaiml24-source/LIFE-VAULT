package com.lifevault.app.presentation.pinlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lifevault.app.data.util.PasswordHasher
import com.lifevault.app.presentation.theme.RoyalPurple

/**
 * Local-only PIN gate shown before the app content when PIN lock is
 * enabled. Checks against the SHA-256 hash stored in DataStore
 * (AppPreferences) — no network call, no Firebase involved.
 */
@Composable
fun PinUnlockScreen(storedPinHash: String, onUnlocked: () -> Unit) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (PasswordHasher.hash(enteredPin) == storedPinHash) {
                onUnlocked()
            } else {
                error = "Incorrect PIN"
                enteredPin = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))
        Icon(Icons.Filled.Lock, contentDescription = null, tint = RoyalPurple, modifier = Modifier.height(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            "Enter your PIN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = enteredPin,
            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) enteredPin = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error != null
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
