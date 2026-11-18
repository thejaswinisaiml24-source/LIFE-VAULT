package com.lifevault.app.presentation.auth.register

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.common.PasswordStrengthIndicator
import com.lifevault.app.presentation.common.evaluatePasswordStrength
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.registrationSucceeded) {
        if (state.registrationSucceeded) {
            kotlinx.coroutines.delay(1400)
            onRegistrationSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        LifeVaultShieldLogo()

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Create Your LifeVault",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Protect your identity, documents and digital legacy.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        LifeVaultTextField(
            value = state.fullName,
            onValueChange = viewModel::onFullNameChange,
            label = "Full Name",
            leadingIcon = Icons.Filled.Badge,
            isError = state.fieldErrors.containsKey("fullName"),
            errorText = state.fieldErrors["fullName"]
        )
        Spacer(Modifier.height(16.dp))

        LifeVaultTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email Address",
            leadingIcon = Icons.Filled.AlternateEmail,
            keyboardType = KeyboardType.Email,
            isError = state.fieldErrors.containsKey("email"),
            errorText = state.fieldErrors["email"]
        )
        Spacer(Modifier.height(16.dp))

        LifeVaultTextField(
            value = state.phoneNumber,
            onValueChange = viewModel::onPhoneChange,
            label = "Phone Number",
            leadingIcon = Icons.Filled.Phone,
            keyboardType = KeyboardType.Phone,
            isError = state.fieldErrors.containsKey("phone"),
            errorText = state.fieldErrors["phone"]
        )
        Spacer(Modifier.height(16.dp))

        LifeVaultTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            isError = state.fieldErrors.containsKey("password"),
            errorText = state.fieldErrors["password"]
        )
        Spacer(Modifier.height(8.dp))
        PasswordStrengthIndicator(strength = evaluatePasswordStrength(state.password))
        Spacer(Modifier.height(16.dp))

        LifeVaultTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm Password",
            leadingIcon = Icons.Filled.LockReset,
            isPassword = true,
            passwordVisible = state.confirmPasswordVisible,
            onTogglePasswordVisibility = viewModel::onToggleConfirmPasswordVisibility,
            isError = state.fieldErrors.containsKey("confirmPassword"),
            errorText = state.fieldErrors["confirmPassword"]
        )

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.agreedToPrivacyPolicy,
                onCheckedChange = { viewModel.onTogglePrivacyPolicy() },
                colors = CheckboxDefaults.colors(checkedColor = RoyalPurple)
            )
            Text(
                text = "Agree to Privacy Policy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (state.fieldErrors.containsKey("privacy")) {
            Text(
                text = state.fieldErrors["privacy"]!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        state.submissionError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LifeVaultGradientButton(
            text = "Create Account",
            onClick = viewModel::submit,
            enabled = state.isFormValid && !state.isLoading,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  or continue with  ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        val launchGoogleSignIn = com.lifevault.app.presentation.common.rememberGoogleSignInLauncher(
            onIdToken = { idToken -> viewModel.onGoogleSignInResult(idToken) }
        )

        OutlinedButton(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { /* Biometric enrollment wired up when BiometricPrompt flow is scoped */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue with Biometrics")
        }

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Login", color = RoyalPurple, fontWeight = FontWeight.SemiBold)
            }
        }
    }

        com.lifevault.app.presentation.common.SuccessToast(
            message = "Registered successfully! Welcome to LifeVault \uD83D\uDC4B",
            visible = state.registrationSucceeded,
            onDismiss = { },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun LifeVaultShieldLogo(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Image(
            painter = painterResource(id = com.lifevault.app.R.drawable.lifevault_logo),
            contentDescription = "LifeVault",
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "LifeVault",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
