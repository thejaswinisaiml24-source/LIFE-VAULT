package com.lifevault.app.presentation.nominee

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.AccentBlue
import com.lifevault.app.presentation.theme.AccentIndigo
import com.lifevault.app.presentation.theme.AccentOrange
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun AddNomineeScreen(
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: AddNomineeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            delay(900)
            onSaveComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Add Trusted Nominee",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Assign someone who can access selected information only during emergencies.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            LifeVaultTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nominee Full Name",
                leadingIcon = Icons.Filled.Badge,
                isError = state.fieldErrors.containsKey("fullName"),
                errorText = state.fieldErrors["fullName"]
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.relationship,
                onValueChange = viewModel::onRelationshipChange,
                label = "Relationship",
                leadingIcon = Icons.Filled.Groups,
                isError = state.fieldErrors.containsKey("relationship"),
                errorText = state.fieldErrors["relationship"]
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
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                leadingIcon = Icons.Filled.AlternateEmail,
                keyboardType = KeyboardType.Email,
                isError = state.fieldErrors.containsKey("email"),
                errorText = state.fieldErrors["email"]
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "Address",
                leadingIcon = Icons.Filled.Home
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.governmentId,
                onValueChange = viewModel::onGovernmentIdChange,
                label = "Government ID",
                leadingIcon = Icons.Filled.CreditCard,
                isError = state.fieldErrors.containsKey("governmentId"),
                errorText = state.fieldErrors["governmentId"]
            )
            Spacer(Modifier.height(16.dp))

            LifeVaultTextField(
                value = state.loginPassword,
                onValueChange = viewModel::onLoginPasswordChange,
                label = "Nominee Login Password",
                leadingIcon = Icons.Filled.Password,
                isPassword = true,
                isError = state.fieldErrors.containsKey("loginPassword"),
                errorText = state.fieldErrors["loginPassword"]
            )
            Text(
                text = "Nominee uses this with their email to sign in",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose exactly what this nominee can access in an emergency.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                permissionMeta.forEach { (permission, meta) ->
                    PermissionRow(
                        label = permission.label,
                        icon = meta.first,
                        accentColor = meta.second,
                        checked = state.permissions[permission] ?: false,
                        onToggle = { viewModel.onTogglePermission(permission) }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            state.saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            LifeVaultGradientButton(
                text = "Save Nominee",
                onClick = viewModel::save,
                enabled = state.isFormValid && !state.isSaving,
                isLoading = state.isSaving
            )

            Spacer(Modifier.height(40.dp))
        }

        AnimatedVisibility(
            visible = state.saveSucceeded,
            enter = fadeIn(tween(200)) + scaleIn(tween(250)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SuccessBadge()
        }
    }
}

@Composable
private fun SuccessBadge() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
            .padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Nominee Added",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PermissionRow(
    label: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = accentColor)
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = RoyalPurple, checkedTrackColor = RoyalPurple.copy(alpha = 0.5f))
        )
    }
}

private val permissionMeta: Map<NomineePermission, Pair<ImageVector, androidx.compose.ui.graphics.Color>> = mapOf(
    NomineePermission.EMERGENCY_PROFILE to (Icons.Filled.Shield to RoyalPurple),
    NomineePermission.MEDICAL_RECORDS to (Icons.Filled.HealthAndSafety to SuccessGreen),
    NomineePermission.IMPORTANT_DOCUMENTS to (Icons.Filled.Description to AccentBlue),
    NomineePermission.DIGITAL_LEGACY to (Icons.Filled.Widgets to AccentIndigo),
    NomineePermission.PASSWORD_VAULT to (Icons.Filled.Password to AccentOrange)
)
