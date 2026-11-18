package com.lifevault.app.presentation.digitallegacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
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
fun DigitalLegacyScreen(
    onNavigateBack: () -> Unit,
    viewModel: DigitalLegacyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (state.selectedCategory != null) viewModel.onBackToCategories() else onNavigateBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = state.selectedCategory?.label ?: "Digital Legacy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (state.selectedCategory != null) {
                TextButton(onClick = viewModel::onOpenAddDialog) {
                    Text("+ Add New", color = RoyalPurple)
                }
            }
        }

        if (state.selectedCategory == null) {
            Text(
                text = "Will be released to nominees after verification",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RoyalPurple)
                }
            }
            state.selectedCategory == null -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegacyCategory.entries.forEach { category ->
                        CategoryCard(
                            category = category,
                            count = state.counts[category] ?: 0,
                            onClick = { viewModel.onSelectCategory(category) }
                        )
                    }
                }
            }
            state.itemsInCategory.isEmpty() -> {
                Text(
                    text = "Nothing added yet in ${state.selectedCategory!!.label}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.itemsInCategory.forEach { item ->
                        ItemCard(item = item, onDelete = { viewModel.deleteItem(item.id) })
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissAddDialog,
            title = { Text("Add to ${state.selectedCategory?.label}") },
            text = {
                Column {
                    LifeVaultTextField(state.newTitle, viewModel::onNewTitleChange, "Title", Icons.Filled.Description, isError = state.error != null, errorText = state.error)
                    Spacer(Modifier.height(10.dp))
                    LifeVaultTextField(state.newNotes, viewModel::onNewNotesChange, "Notes / Details", Icons.Filled.Description, singleLine = false)
                    if (state.selectedCategory == LegacyCategory.PHOTOS_VIDEOS) {
                        Spacer(Modifier.height(10.dp))
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                        ) { uri: android.net.Uri? ->
                            uri?.let { viewModel.onFilePicked(it, context.contentResolver) }
                        }
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { filePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                state.pickedFileName ?: "Tap to select a photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addItem, enabled = !state.isSaving) {
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
private fun CategoryCard(category: LegacyCategory, count: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
                Icon(Icons.Filled.Description, contentDescription = null, tint = RoyalPurple)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("$count items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ItemCard(item: LegacyItem, onDelete: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (item.notes.isNotBlank()) {
                    Text(item.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
