package com.lifevault.app.presentation.editprofile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.common.SuccessToast
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            kotlinx.coroutines.delay(1000)
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Edit Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RoyalPurple)
                }
                return@Column
            }

            Spacer(Modifier.height(24.dp))

            LifeVaultTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Full Name",
                leadingIcon = Icons.Filled.Badge,
                isError = state.error != null,
                errorText = state.error
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number",
                leadingIcon = Icons.Filled.Phone,
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.email,
                onValueChange = {},
                label = "Email (cannot be changed)",
                leadingIcon = Icons.Filled.AlternateEmail
            )

            Spacer(Modifier.height(28.dp))

            LifeVaultGradientButton(
                text = "Save Changes",
                onClick = viewModel::save,
                enabled = !state.isSaving,
                isLoading = state.isSaving
            )
        }

        SuccessToast(
            message = "Profile updated successfully",
            visible = state.saveSucceeded,
            onDismiss = viewModel::consumeSaveSuccess,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
