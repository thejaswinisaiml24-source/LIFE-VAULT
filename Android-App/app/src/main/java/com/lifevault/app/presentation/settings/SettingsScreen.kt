package com.lifevault.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.theme.RoyalPurple

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onInfoPageClick: (String) -> Unit,
    onPinLockClick: () -> Unit,
    onLogoutClick: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsRow(Icons.Filled.Person, "Edit Profile", onClick = onEditProfileClick)
        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.Lock, "App Lock (PIN)", onClick = onPinLockClick)
        Spacer(Modifier.height(10.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DarkMode, contentDescription = null, tint = RoyalPurple)
                    Spacer(Modifier.width(14.dp))
                    Text("Dark Theme", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { themeViewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = RoyalPurple, checkedTrackColor = RoyalPurple.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.Info, "About LifeVault") { onInfoPageClick("about") }
        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.HelpOutline, "Help") { onInfoPageClick("help") }
        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.PrivacyTip, "Privacy Policy") { onInfoPageClick("privacy") }
        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.Gavel, "Terms of Service") { onInfoPageClick("terms") }
        Spacer(Modifier.height(10.dp))
        SettingsRow(Icons.Filled.Description, "Send Feedback") { onInfoPageClick("feedback") }

        Spacer(Modifier.height(24.dp))

        SettingsRow(Icons.AutoMirrored.Filled.Logout, "Logout", isDestructive = true, onClick = onLogoutClick)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = if (isDestructive) MaterialTheme.colorScheme.error else RoyalPurple)
                Spacer(Modifier.width(14.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            if (!isDestructive) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            }
        }
    }
}
