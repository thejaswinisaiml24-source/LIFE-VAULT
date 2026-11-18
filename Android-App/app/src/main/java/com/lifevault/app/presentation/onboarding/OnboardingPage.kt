package com.lifevault.app.presentation.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sos
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.Shield,
        title = "Your Digital Life, Protected",
        description = "Store your important documents, emergency profile and medical records securely in one protected place."
    ),
    OnboardingPage(
        icon = Icons.Filled.Sos,
        title = "Emergency Help In Seconds",
        description = "Send instant SOS alerts, live location and emergency details to your trusted contacts."
    ),
    OnboardingPage(
        icon = Icons.Filled.GroupAdd,
        title = "Trusted Nominee Access",
        description = "Allow your trusted nominee to securely access essential information during emergencies after verification."
    )
)
