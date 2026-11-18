package com.lifevault.app.presentation.auth.forgotpassword

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.SuccessGreen

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(40.dp))

        if (state.emailSent) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MarkEmailRead, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Check your email",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "We sent a password reset link to ${state.email}. Open the email, tap the link, set a new password, then come back and log in. If you can't see it, check your Spam folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_APP_EMAIL)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // No email app available.
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Email App")
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = viewModel::sendResetLink,
                    enabled = state.resendSecondsLeft == 0 && !state.isSending
                ) {
                    Text(
                        if (state.resendSecondsLeft > 0) "Resend link in ${state.resendSecondsLeft}s" else "Resend link",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                TextButton(onClick = viewModel::onUseDifferentEmail) {
                    Text("Use a different email", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onNavigateBack) {
                    Text("Back to Login", color = RoyalPurple, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Text(
                text = "Enter your account email and we'll send you a link to reset your password.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            LifeVaultTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                leadingIcon = Icons.Filled.AlternateEmail,
                keyboardType = KeyboardType.Email,
                isError = state.errorMessage != null,
                errorText = state.errorMessage
            )

            Spacer(Modifier.height(28.dp))

            LifeVaultGradientButton(
                text = "Send Reset Link",
                onClick = viewModel::sendResetLink,
                enabled = state.email.isNotBlank() && !state.isSending,
                isLoading = state.isSending
            )

            Spacer(Modifier.height(20.dp))

            Row {
                Text("Remembered your password? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onNavigateBack) {
                    Text("Back to Login", color = RoyalPurple, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
