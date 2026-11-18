package com.lifevault.app.presentation.emergencyprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun EmergencyProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EmergencyProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Emergency Profile",
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

        Spacer(Modifier.height(16.dp))

        if (!state.isEditing) {
            // Read-only summary view
            Column {
                SummaryRow("Blood Group", state.bloodGroup)
                SummaryRow("Age", state.age)
                SummaryRow("Gender", state.gender)
                SummaryRow("Height", state.height)
                SummaryRow("Weight", state.weight)
                SummaryRow("Organ Donor", if (state.organDonor) "Yes" else "No")
                SummaryRow("Allergies", state.allergies)
                SummaryRow("Medical Conditions", state.medicalConditions)
                SummaryRow("Current Medicines", state.currentMedicines)
                SummaryRow("Emergency Notes", state.emergencyNotes)
                SummaryRow("Emergency Contact", "${state.emergencyContactName} (${state.emergencyContactPhone})")

                Spacer(Modifier.height(24.dp))
                LifeVaultGradientButton(text = "Edit Profile", onClick = viewModel::onEnableEdit)
            }
        } else {
            LifeVaultTextField(state.bloodGroup, viewModel::onBloodGroupChange, "Blood Group", Icons.Filled.Bloodtype)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.age, viewModel::onAgeChange, "Age", Icons.Filled.Cake, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.gender, viewModel::onGenderChange, "Gender", Icons.Filled.Person)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.height, viewModel::onHeightChange, "Height (cm)", Icons.Filled.Height, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.weight, viewModel::onWeightChange, "Weight (kg)", Icons.Filled.MonitorWeight, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.organDonor,
                    onCheckedChange = { viewModel.onToggleOrganDonor() },
                    colors = CheckboxDefaults.colors(checkedColor = RoyalPurple)
                )
                Text("Organ Donor", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))

            LifeVaultTextField(state.allergies, viewModel::onAllergiesChange, "Allergies", Icons.Filled.Warning, singleLine = false)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.medicalConditions, viewModel::onMedicalConditionsChange, "Medical Conditions", Icons.Filled.Warning, singleLine = false)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.currentMedicines, viewModel::onCurrentMedicinesChange, "Current Medicines", Icons.Filled.Warning, singleLine = false)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.emergencyNotes, viewModel::onEmergencyNotesChange, "Emergency Notes", Icons.Filled.Warning, singleLine = false)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.emergencyContactName, viewModel::onEmergencyContactNameChange, "Emergency Contact Name", Icons.Filled.ContactPhone)
            Spacer(Modifier.height(12.dp))
            LifeVaultTextField(state.emergencyContactPhone, viewModel::onEmergencyContactPhoneChange, "Emergency Contact Phone", Icons.Filled.Phone, keyboardType = KeyboardType.Phone)

            Spacer(Modifier.height(24.dp))
            state.saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
            }
            LifeVaultGradientButton(
                text = "Save",
                onClick = viewModel::save,
                enabled = !state.isSaving,
                isLoading = state.isSaving
            )
        }

        Spacer(Modifier.height(40.dp))
    }

        com.lifevault.app.presentation.common.SuccessToast(
            message = "Emergency Profile saved successfully",
            visible = state.saveSucceeded,
            onDismiss = viewModel::consumeSaveSuccess,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
