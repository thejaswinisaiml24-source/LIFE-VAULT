package com.lifevault.app.presentation.documents

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
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
import com.lifevault.app.presentation.common.LifeVaultTextField
import com.lifevault.app.presentation.theme.RoyalPurple

private val documentTabs = listOf("All", "Personal", "Financial", "Others")

@Composable
fun DocumentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredDocs = if (selectedTab == 0) {
        state.documents
    } else {
        state.documents.filter { it.category.equals(documentTabs[selectedTab], ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    text = "Documents",
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
            documentTabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RoyalPurple)
                    }
                }
                filteredDocs.isEmpty() -> {
                    Text(
                        text = "No documents in this category yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredDocs.forEach { doc ->
                            DocumentCard(doc = doc, onDelete = { viewModel.deleteDocument(doc.id) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            androidx.compose.material3.OutlinedButton(
                onClick = viewModel::onOpenAddDialog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Upload Document")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (state.showAddDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            uri?.let { viewModel.onFilePicked(it, context.contentResolver) }
        }

        AlertDialog(
            onDismissRequest = viewModel::onDismissAddDialog,
            title = { Text("Upload Document") },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { filePickerLauncher.launch("*/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = RoyalPurple)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                state.pickedFileName ?: "Tap to upload",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LifeVaultTextField(state.newTitle, viewModel::onNewTitleChange, "Document Name", Icons.Filled.Description, isError = state.error != null, errorText = state.error)
                    Spacer(Modifier.height(12.dp))
                    LifeVaultTextField(state.newCategory, viewModel::onNewCategoryChange, "Category (Personal / Financial / Others)", Icons.Filled.Description)
                    Spacer(Modifier.height(12.dp))
                    LifeVaultTextField(state.newNotes, viewModel::onNewNotesChange, "Notes", Icons.Filled.Description, singleLine = false)
                    Spacer(Modifier.height(12.dp))
                    LifeVaultTextField(state.newExpiryDate, viewModel::onNewExpiryDateChange, "Expiry Date (optional, e.g. dd/MM/yyyy)", Icons.Filled.Description)
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::addDocument, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Uploading..." else "Upload", color = RoyalPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissAddDialog) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DocumentCard(doc: DocumentRecord, onDelete: () -> Unit) {
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
                Icon(Icons.Filled.Description, contentDescription = null, tint = RoyalPurple)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (doc.category.isNotBlank()) {
                    Text(doc.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
