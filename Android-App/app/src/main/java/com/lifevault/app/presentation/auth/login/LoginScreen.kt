package com.lifevault.app.presentation.auth.login

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.auth.register.LifeVaultShieldLogo
import com.lifevault.app.presentation.common.GlassCard
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToNomineeLogin: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.loginSucceeded) {
        if (state.loginSucceeded) {
            kotlinx.coroutines.delay(1200)
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        LifeVaultShieldLogo()

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(28.dp))

        LifeVaultTextField(
            value = state.emailOrPhone,
            onValueChange = viewModel::onEmailOrPhoneChange,
            label = "Email or Phone",
            leadingIcon = Icons.Filled.AlternateEmail,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))

        LifeVaultTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            passwordVisible = state.passwordVisible,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility
        )

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Forgot Password", color = RoyalPurple)
            }
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        LifeVaultGradientButton(
            text = "Login",
            onClick = viewModel::submit,
            enabled = state.isFormValid && !state.isLoading,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateToNomineeLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Nominee Login")
        }

        Spacer(Modifier.height(16.dp))

        val context = androidx.compose.ui.platform.LocalContext.current
        var biometricError by remember { mutableStateOf<String?>(null) }

        OutlinedButton(
            onClick = {
                val activity = context as? androidx.fragment.app.FragmentActivity
                if (activity == null) {
                    biometricError = "Biometric login isn't available here"
                    return@OutlinedButton
                }
                when (com.lifevault.app.presentation.common.checkBiometricAvailability(activity)) {
                    com.lifevault.app.presentation.common.BiometricAvailability.Available -> {
                        val hasCachedSession = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
                        if (!hasCachedSession) {
                            biometricError = "Login with your password once first to enable biometric login"
                        } else {
                            com.lifevault.app.presentation.common.showBiometricPrompt(
                                activity = activity,
                                onSuccess = { onLoginSuccess() },
                                onError = { message -> biometricError = message }
                            )
                        }
                    }
                    com.lifevault.app.presentation.common.BiometricAvailability.NoneEnrolled ->
                        biometricError = "No fingerprint/face enrolled on this device"
                    com.lifevault.app.presentation.common.BiometricAvailability.NoHardware ->
                        biometricError = "This device has no biometric hardware"
                    com.lifevault.app.presentation.common.BiometricAvailability.Unavailable ->
                        biometricError = "Biometric login isn't available right now"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Biometric Login")
        }
        biometricError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  or  ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        val launchGoogleSignIn = com.lifevault.app.presentation.common.rememberGoogleSignInLauncher(
            onIdToken = { idToken -> viewModel.onGoogleSignInResult(idToken) }
        )

        OutlinedButton(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }

        Spacer(Modifier.height(32.dp))

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassCardItem(icon = Icons.Filled.Shield, label = "Your Data")
                GlassCardItem(icon = Icons.Filled.VerifiedUser, label = "Your Identity")
                GlassCardItem(icon = Icons.Filled.AllInclusive, label = "Your Legacy")
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "New to LifeVault? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Register", color = RoyalPurple, fontWeight = FontWeight.SemiBold)
            }
        }
    }

        com.lifevault.app.presentation.common.SuccessToast(
            message = "Login successful! Welcome back \uD83D\uDC4B",
            visible = state.loginSucceeded,
            onDismiss = { },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun GlassCardItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = RoyalPurple)
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
