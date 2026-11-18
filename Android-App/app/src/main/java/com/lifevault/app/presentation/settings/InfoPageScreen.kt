package com.lifevault.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifevault.app.presentation.theme.RoyalPurple

private val pageContent = mapOf(
    "about" to ("About LifeVault" to "LifeVault is a personal emergency and digital legacy management app. It centralizes emergency medical information, important documents, insurance details, and nominee access on one secure platform, built with Kotlin, Jetpack Compose, and Firebase."),
    "help" to ("User Manual" to "For help using LifeVault: use the Dashboard to reach any feature. Add your Emergency Profile and Medical Records first so nominees have something to see. Use Add Nominee to grant trusted people scoped access to your information."),
    "privacy" to ("Privacy Policy" to "Your data is stored in Firebase Firestore and is only accessible to your authenticated account. Nominees you approve can access only the specific data categories you grant them. This app does not sell or share your data with third parties."),
    "terms" to ("Terms of Service" to "LifeVault is provided as-is for personal emergency preparedness use. It is not a substitute for professional medical, legal, or emergency services. Always contact real emergency services in a genuine emergency."),
    "feedback" to ("Send Feedback" to "We'd love to hear from you. Use your device's email app to send feedback, bug reports, or feature requests to the development team.")
)

private data class ManualSection(val title: String, val body: String)

private const val MANUAL_INTRO =
    "Welcome to LifeVault. This guide explains every part of the app, step by step, so you and the people you trust are ready when it matters."

private val userManual = listOf(
    ManualSection(
        "1. Getting Started",
        "Open the app and swipe through the Onboarding screens (or tap Skip). Tap Register, enter your details and create an account, then log in with your email and password. " +
            "If you enable PIN Lock in Settings, the app asks for your PIN every time it opens."
    ),
    ManualSection(
        "2. Forgot Password / Reset Password",
        "On the Login screen tap Forgot Password and enter your account email. Tap Send Reset Link - Firebase emails you a secure link. " +
            "Open the email (check Spam or Promotions if you cannot see it), tap the link, type a new password and save. Then return to the app and log in with the new password. " +
            "You can request a new link after 30 seconds."
    ),
    ManualSection(
        "3. Dashboard",
        "The Dashboard is your home screen. The SOS button, Alerts bell and Profile icon are at the top. Below them are quick-access tiles for every feature: Emergency Profile, Medical Records, Important Documents, Medicine Reminder, Insurance, Digital Legacy, Password Vault, Storage Summary, Trusted Contacts, QR Emergency Card, Nominee Management, AI Health Assistant and Settings."
    ),
    ManualSection(
        "4. Emergency SOS",
        "Add at least one Trusted Contact or Nominee first. In an emergency tap the SOS button on the Dashboard, then tap the big red button. " +
            "Allow Location and SMS permissions the first time. LifeVault gets your live location and immediately sends an SMS with a Google Maps link to all your trusted contacts and nominees. " +
            "While SOS is active your location keeps updating (nominees see it live on their dashboard, and a fresh SMS goes out every 2 minutes). " +
            "Keep the screen open and GPS turned on. Tap I'm Safe - Stop SOS when you no longer need help; contacts receive a safe message and you can rate your experience. " +
            "The Call Police (100) and Call Ambulance (108) buttons open your phone dialer."
    ),
    ManualSection(
        "5. Emergency Profile",
        "Store the details responders need first: blood group, allergies, medical conditions and your emergency contact. Fill it in once and tap Save. Update it whenever something changes."
    ),
    ManualSection(
        "6. Medical Records",
        "Record your doctor, hospital and current medications in one place. Nominees with permission can view this in an emergency."
    ),
    ManualSection(
        "7. Important Documents",
        "Keep a list of your important documents such as IDs, certificates and property papers. Add a title and details, and delete entries you no longer need."
    ),
    ManualSection(
        "8. Medicine Reminder",
        "Add each medicine with its time and LifeVault notifies you when it is due. Allow notifications when asked so reminders are not missed."
    ),
    ManualSection(
        "9. Insurance",
        "Save your insurance policies (health, life, vehicle and others) with provider and policy details so family can find them quickly."
    ),
    ManualSection(
        "10. Digital Legacy",
        "Record your wishes and digital accounts you want handled for your family, so your loved ones know what to do."
    ),
    ManualSection(
        "11. Password Vault",
        "Save website and app logins securely inside LifeVault. Use it instead of paper notes and keep your PIN Lock on."
    ),
    ManualSection(
        "12. Storage Summary / Secure Vault",
        "See an overview of what you have stored across LifeVault and how much space your data uses."
    ),
    ManualSection(
        "13. Trusted Contacts",
        "Open Trusted Contacts from the Dashboard, tap Add, then enter name, phone number and relationship. These people receive your live location by SMS when you press SOS. Use the delete option to remove a contact."
    ),
    ManualSection(
        "14. Nominee Management",
        "Open Nominee Management, enter your nominee's name, relationship, phone, email, government ID and a login password, then choose exactly which sections they may see (Emergency Profile, Medical Records, Important Documents, Digital Legacy, Password Vault). " +
            "Share the email and password with your nominee. They open the app, choose Nominee Login and sign in to view only the sections you allowed. Nominees also receive your SOS alerts."
    ),
    ManualSection(
        "15. QR Emergency Card",
        "Generate a QR code with your emergency information. Keep it on your phone or print it so responders can scan it quickly."
    ),
    ManualSection(
        "16. AI Health Assistant",
        "Ask general health and wellness questions in a chat. It gives general guidance only and is not a replacement for a doctor."
    ),
    ManualSection(
        "17. Profile and Edit Profile",
        "Tap the Profile icon on the Dashboard to see your details. Use Edit Profile (also in Settings) to change your name and other information."
    ),
    ManualSection(
        "18. Settings",
        "In Settings you can edit your profile, switch between dark and light theme, turn PIN Lock on or off, read About, this User Manual, the Privacy Policy and Terms, send feedback, and log out."
    ),
    ManualSection(
        "19. Send Feedback",
        "Go to Settings > Send Feedback, tap 1 to 5 stars, optionally write a review and tap Submit. You are also asked for a rating after you stop an SOS."
    ),
    ManualSection(
        "20. Troubleshooting",
        "SOS location not found: turn on GPS and allow Location permission, then try again outdoors or near a window.\n" +
            "SMS not sent: allow SMS permission, make sure your SIM has balance and signal, and check the contact numbers.\n" +
            "No reset email: check Spam, confirm the email is the one you registered with, then use Resend after 30 seconds.\n" +
            "Forgot your PIN: log out and sign in again with your password, then set a new PIN in Settings."
    )
)

@Composable
fun InfoPageScreen(pageKey: String, onNavigateBack: () -> Unit) {
    val (title, body) = pageContent[pageKey] ?: ("LifeVault" to "")
    val isManual = pageKey == "help"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(20.dp))

        if (isManual) {
            Text(
                text = MANUAL_INTRO,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            userManual.forEach { section ->
                Spacer(Modifier.height(20.dp))
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalPurple
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = section.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(24.dp))
        } else {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
