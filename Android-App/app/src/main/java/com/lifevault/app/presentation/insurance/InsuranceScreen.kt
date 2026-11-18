package com.lifevault.app.presentation.insurance

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun InsuranceScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsuranceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Insurance",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            TextButton(onClick = viewModel::onOpenAddDialog) {
                Text("+ Add New", color = RoyalPurple)
            }
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RoyalPurple)
                }
            }
            state.policies.isEmpty() -> {
                Text(
                    text = "No insurance policies added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.policies.forEach { policy ->
                        InsuranceCard(policy = policy, onDelete = { viewModel.deletePolicy(policy.id) })
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissAddDialog,
            title = { Text("Add Insurance Policy") },
            text = {
                Column {
                    LifeVaultTextField(state.newType, viewModel::onNewTypeChange, "Type (e.g. Health Insurance)", Icons.Filled.Shield, isError = state.error != null, errorText = state.error)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newCompany, viewModel::onNewCompanyChange, "Company", Icons.Filled.Shield)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newPolicyNumber, viewModel::onNewPolicyNumberChange, "Policy Number", Icons.Filled.Shield)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newValidTill, viewModel::onNewValidTillChange, "Valid Till", Icons.Filled.Shield)
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addPolicy, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Saving..." else "Save", color = RoyalPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissAddDialog) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun InsuranceCard(policy: InsuranceRecord, onDelete: () -> Unit) {
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
                modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small).background(RoyalPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = RoyalPurple)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(policy.type, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(policy.company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (policy.policyNumber.isNotBlank()) {
                    Text("Policy: ${policy.policyNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (policy.validTill.isNotBlank()) {
                    Text("Valid till: ${policy.validTill}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
