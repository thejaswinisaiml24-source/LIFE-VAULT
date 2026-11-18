package com.lifevault.app.presentation.trustedcontacts

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple
import androidx.compose.material.icons.filled.Add

@Composable
fun TrustedContactsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrustedContactsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onOpenAddDialog, containerColor = RoyalPurple) {
                Icon(Icons.Filled.Add, contentDescription = "Add contact")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Trusted Contacts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RoyalPurple)
                    }
                }
                state.contacts.isEmpty() -> {
                    Text(
                        "No trusted contacts added yet. Tap + to add someone you trust.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.contacts.forEach { contact ->
                            ContactCard(
                                contact = contact,
                                onCall = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}")))
                                },
                                onDelete = { viewModel.deleteContact(contact.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissAddDialog,
            title = { Text("Add Trusted Contact") },
            text = {
                Column {
                    LifeVaultTextField(state.newName, viewModel::onNewNameChange, "Name", Icons.Filled.Person, isError = state.error != null, errorText = state.error)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newPhone, viewModel::onNewPhoneChange, "Phone Number", Icons.Filled.Call)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newRelationship, viewModel::onNewRelationshipChange, "Relationship", Icons.Filled.Person)
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addContact, enabled = !state.isSaving) {
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
private fun ContactCard(contact: TrustedContact, onCall: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(RoyalPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = RoyalPurple)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("${contact.relationship} - ${contact.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = RoyalPurple)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
