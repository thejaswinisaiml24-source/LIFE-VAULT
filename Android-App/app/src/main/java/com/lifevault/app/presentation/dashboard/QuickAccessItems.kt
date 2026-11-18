package com.lifevault.app.presentation.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lifevault.app.presentation.theme.AccentBlue
import com.lifevault.app.presentation.theme.AccentCyan
import com.lifevault.app.presentation.theme.AccentIndigo
import com.lifevault.app.presentation.theme.AccentOrange
import com.lifevault.app.presentation.theme.AccentPink
import com.lifevault.app.presentation.theme.AccentTeal
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.SuccessGreen

data class QuickAccessItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

val quickAccessItems = listOf(
    QuickAccessItem("emergency_profile", "Emergency Profile", Icons.Filled.Shield, RoyalPurple),
    QuickAccessItem("medical_records", "Medical Records", Icons.Filled.HealthAndSafety, SuccessGreen),
    QuickAccessItem("important_documents", "Important Documents", Icons.Filled.Description, AccentBlue),
    QuickAccessItem("medicine_reminder", "Medicine Reminder", Icons.Filled.Medication, AccentCyan),
    QuickAccessItem("insurance", "Insurance", Icons.Filled.Shield, AccentOrange),
    QuickAccessItem("digital_legacy", "Digital Legacy", Icons.Filled.Widgets, AccentIndigo),
    QuickAccessItem("password_vault", "Password Vault", Icons.Filled.Password, AccentPink),
    QuickAccessItem("storage_summary", "Storage Summary", Icons.Filled.Shield, AccentCyan),
    QuickAccessItem("trusted_contacts", "Trusted Contacts", Icons.Filled.Contacts, AccentTeal),
    QuickAccessItem("qr_emergency_card", "QR Emergency Card", Icons.Filled.QrCode2, AccentCyan),
    QuickAccessItem("nominee_management", "Nominee Management", Icons.Filled.Groups, RoyalPurple),
    QuickAccessItem("ai_assistant", "AI Health Assistant", Icons.Filled.SmartToy, AccentIndigo),
    QuickAccessItem("settings", "Settings", Icons.Filled.Settings, AccentTeal)
)
