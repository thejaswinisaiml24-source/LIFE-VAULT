package com.lifevault.app.presentation.nominee

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.theme.ErrorRed
import com.lifevault.app.presentation.theme.RoyalPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NomineeDashboardScreen(
    onLogout: () -> Unit,
    viewModel: NomineeDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Nominee Access", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    state.nomineeName.ifBlank { "..." },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = {
                viewModel.logout()
                onLogout()
            }) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalPurple)
            }
            return@Column
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            return@Column
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Viewing information shared by ${state.ownerName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (state.sosActive) {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "SOS ALERT - ${state.ownerName} needs help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    state.sosUpdatedAt?.let { updated ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Live location updated at " + SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(updated)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val mapsUrl = state.sosMapsUrl
                    if (mapsUrl != null) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
                                } catch (e: Exception) {
                                    // No app can open the map link.
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("Open Live Location")
                        }
                    } else {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Waiting for location...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        if (state.emergencyProfile != null) {
            SectionCard(title = "Emergency Profile", icon = Icons.Filled.Shield) {
                val p = state.emergencyProfile!!
                InfoLine("Blood Group", p["bloodGroup"]?.toString())
                InfoLine("Allergies", p["allergies"]?.toString())
                InfoLine("Medical Conditions", p["medicalConditions"]?.toString())
                InfoLine("Emergency Contact", "${p["emergencyContactName"] ?: ""} (${p["emergencyContactPhone"] ?: ""})")
            }
            Spacer(Modifier.height(14.dp))
        }

        if (state.medicalRecords != null) {
            SectionCard(title = "Medical Records", icon = Icons.Filled.HealthAndSafety) {
                val m = state.medicalRecords!!
                InfoLine("Doctor", m["doctorName"]?.toString())
                InfoLine("Hospital", m["hospitalName"]?.toString())
                InfoLine("Current Medications", m["currentMedications"]?.toString())
            }
            Spacer(Modifier.height(14.dp))
        }

        if (state.documentTitles.isNotEmpty()) {
            SectionCard(title = "Documents", icon = Icons.Filled.Description) {
                state.documentTitles.forEach { title ->
                    Text(
                        "• $title",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        if (state.emergencyProfile == null && state.medicalRecords == null && state.documentTitles.isEmpty()) {
            Text(
                "No information has been shared with you yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = RoyalPurple)
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
