package com.lifevault.app.presentation.passwordvault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun PasswordVaultScreen(
    onNavigateBack: () -> Unit,
    viewModel: PasswordVaultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalPurple)
        }
        return
    }

    if (!state.isUnlocked) {
        VaultLockScreen(state = state, onNavigateBack = onNavigateBack, viewModel = viewModel)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddDialog, containerColor = RoyalPurple) {
                Icon(Icons.Filled.Add, contentDescription = "Add password")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Password Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.entries.isEmpty()) {
                Text(
                    "No saved passwords yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.entries.forEach { entry ->
                        VaultEntryCard(
                            entry = entry,
                            onToggleReveal = { viewModel.toggleReveal(entry.id) },
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissAddDialog,
            title = { Text("Add Password") },
            text = {
                Column {
                    LifeVaultTextField(state.newSiteName, viewModel::onNewSiteNameChange, "Site / App Name", Icons.Filled.Password, isError = state.error != null, errorText = state.error)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newUsername, viewModel::onNewUsernameChange, "Username / Email", Icons.Filled.Password)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newPassword, viewModel::onNewPasswordChange, "Password", Icons.Filled.Password, isPassword = true)
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addEntry, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Encrypting..." else "Save", color = RoyalPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissAddDialog) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun VaultLockScreen(
    state: PasswordVaultUiState,
    onNavigateBack: () -> Unit,
    viewModel: PasswordVaultViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Password Vault",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(32.dp))
        Icon(Icons.Filled.Lock, contentDescription = null, tint = RoyalPurple, modifier = Modifier.height(48.dp))
        Spacer(Modifier.height(16.dp))

        if (!state.isVaultSetUp) {
            Text(
                "Set up your Vault Master Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This is separate from your login password and encrypts every entry. If you forget it, saved passwords cannot be recovered — there is no reset.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            LifeVaultTextField(state.masterPasswordInput, viewModel::onMasterPasswordChange, "Master Password", Icons.Filled.Lock, isPassword = true)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.confirmMasterPasswordInput, viewModel::onConfirmMasterPasswordChange, "Confirm Master Password", Icons.Filled.Lock, isPassword = true)
            state.unlockError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            LifeVaultGradientButton(text = "Set Up Vault", onClick = viewModel::setUpVault)
        } else {
            Text(
                "Enter your Vault Master Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(20.dp))
            LifeVaultTextField(state.masterPasswordInput, viewModel::onMasterPasswordChange, "Master Password", Icons.Filled.Lock, isPassword = true)
            state.unlockError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
            LifeVaultGradientButton(text = "Unlock", onClick = viewModel::unlock)
        }
    }
}

@Composable
private fun VaultEntryCard(entry: VaultEntry, onToggleReveal: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(entry.siteName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (entry.username.isNotBlank()) {
                        Text(entry.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
                    IconButton(onClick = onToggleReveal) {
                        Icon(
                            if (entry.revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle reveal",
                            tint = RoyalPurple
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (entry.revealed) {
                Text(
                    text = entry.decryptedPassword ?: "Decryption failed",
                    style = MaterialTheme.typography.bodyLarge,
                    color = RoyalPurple,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
