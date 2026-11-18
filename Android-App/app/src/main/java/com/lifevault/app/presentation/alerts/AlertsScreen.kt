package com.lifevault.app.presentation.alerts

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.dashboard.DashboardViewModel
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reuses DashboardViewModel's already-loaded data (medicines + activities)
 * instead of a separate notification/alert storage system, since there's
 * no real push-notification backend wired up — this is a live view of the
 * same Firestore data, not a distinct "notifications" feature.
 */
@Composable
fun AlertsScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Alerts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalPurple)
            }
            return@Column
        }

        if (state.upcomingMedicines.isEmpty() && state.recentActivities.isEmpty() && state.expiryAlerts.isEmpty()) {
            Text(
                "No alerts right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        if (state.expiryAlerts.isNotEmpty()) {
            Text("Expiring Soon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.expiryAlerts.forEach { alert ->
                    val subtitle = if (alert.daysRemaining < 0) {
                        "Expired on ${alert.expiryText}"
                    } else {
                        "Expires ${alert.expiryText} (${alert.daysRemaining} days left)"
                    }
                    AlertCard(
                        icon = Icons.Filled.Warning,
                        iconColor = MaterialTheme.colorScheme.error,
                        title = alert.label,
                        subtitle = subtitle
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        if (state.upcomingMedicines.isNotEmpty()) {
            Text("Medicine Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.upcomingMedicines.forEach { med ->
                    AlertCard(
                        icon = Icons.Filled.Medication,
                        iconColor = SuccessGreen,
                        title = med.name,
                        subtitle = "${med.dose}${if (med.time.isNotBlank()) " at ${med.time}" else ""}"
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        if (state.recentActivities.isNotEmpty()) {
            Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recentActivities.forEach { activity ->
                    val formatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(activity.timestampEpochMillis))
                    AlertCard(
                        icon = Icons.Filled.History,
                        iconColor = RoyalPurple,
                        title = activity.description,
                        subtitle = formatted
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
