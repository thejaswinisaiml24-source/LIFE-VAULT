package com.lifevault.app.presentation.medicalrecords

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple

private val medicalTabs = listOf("All", "Doctor & Hospital", "Conditions", "History")

@Composable
fun MedicalRecordsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicalRecordsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Medical Records",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = RoyalPurple
        ) {
            medicalTabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalPurple)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            val rows = when (selectedTab) {
                1 -> listOf(
                    Triple("Doctor", state.doctorName, Icons.Filled.Person),
                    Triple("Hospital", state.hospitalName, Icons.Filled.LocalHospital)
                )
                2 -> listOf(
                    Triple("Chronic Conditions", state.chronicConditions, Icons.Filled.MedicalServices),
                    Triple("Current Medications", state.currentMedications, Icons.Filled.Medication)
                )
                3 -> listOf(
                    Triple("Vaccination History", state.vaccinationHistory, Icons.Filled.Vaccines),
                    Triple("Medical History Notes", state.medicalHistoryNotes, Icons.Filled.Notes)
                )
                else -> listOf(
                    Triple("Doctor", state.doctorName, Icons.Filled.Person),
                    Triple("Hospital", state.hospitalName, Icons.Filled.LocalHospital),
                    Triple("Chronic Conditions", state.chronicConditions, Icons.Filled.MedicalServices),
                    Triple("Current Medications", state.currentMedications, Icons.Filled.Medication),
                    Triple("Vaccination History", state.vaccinationHistory, Icons.Filled.Vaccines),
                    Triple("Medical History Notes", state.medicalHistoryNotes, Icons.Filled.Notes)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (label, value, icon) ->
                    RecordCard(label = label, value = value, icon = icon)
                }
            }

            Spacer(Modifier.height(20.dp))
            LifeVaultGradientButton(text = "Edit Records", onClick = viewModel::onEnableEdit)
            Spacer(Modifier.height(24.dp))
        }
    }

    if (state.isEditing) {
        EditMedicalRecordsDialog(state = state, viewModel = viewModel)
    }

        com.lifevault.app.presentation.common.SuccessToast(
            message = "Medical Records saved successfully",
            visible = state.saveSucceeded,
            onDismiss = viewModel::consumeSaveSuccess,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun RecordCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(RoyalPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = RoyalPurple)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value.ifBlank { "Not set" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EditMedicalRecordsDialog(state: MedicalRecordsUiState, viewModel: MedicalRecordsViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::onCancelEdit,
        title = { Text("Edit Medical Records") },
        text = {
            Column {
                LifeVaultTextField(state.doctorName, viewModel::onDoctorNameChange, "Doctor Name", Icons.Filled.Person)
                Spacer(Modifier.height(10.dp))
                LifeVaultTextField(state.hospitalName, viewModel::onHospitalNameChange, "Hospital Name", Icons.Filled.LocalHospital)
                Spacer(Modifier.height(10.dp))
                LifeVaultTextField(state.chronicConditions, viewModel::onChronicConditionsChange, "Chronic Conditions", Icons.Filled.MedicalServices, singleLine = false)
                Spacer(Modifier.height(10.dp))
                LifeVaultTextField(state.currentMedications, viewModel::onCurrentMedicationsChange, "Current Medications", Icons.Filled.Medication, singleLine = false)
                Spacer(Modifier.height(10.dp))
                LifeVaultTextField(state.vaccinationHistory, viewModel::onVaccinationHistoryChange, "Vaccination History", Icons.Filled.Vaccines, singleLine = false)
                Spacer(Modifier.height(10.dp))
                LifeVaultTextField(state.medicalHistoryNotes, viewModel::onMedicalHistoryNotesChange, "Medical History Notes", Icons.Filled.Notes, singleLine = false)
                state.saveError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::save, enabled = !state.isSaving) {
                Text(if (state.isSaving) "Saving..." else "Save", color = RoyalPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::onCancelEdit) { Text("Cancel") }
        }
    )
}
