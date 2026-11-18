package com.lifevault.app.presentation.navigation

sealed class LifeVaultDestination(val route: String) {
    data object Splash : LifeVaultDestination("splash")
    data object Onboarding : LifeVaultDestination("onboarding")
    data object Register : LifeVaultDestination("register")
    data object Login : LifeVaultDestination("login")
    data object ForgotPassword : LifeVaultDestination("forgot_password")
    data object NomineeLogin : LifeVaultDestination("nominee_login")
    data object NomineeDashboard : LifeVaultDestination("nominee_dashboard")
    data object Dashboard : LifeVaultDestination("dashboard")
    data object AddNominee : LifeVaultDestination("add_nominee")
    data object EmergencyProfile : LifeVaultDestination("emergency_profile")
    data object MedicalRecords : LifeVaultDestination("medical_records")
    data object Documents : LifeVaultDestination("documents")
    data object MedicineReminder : LifeVaultDestination("medicine_reminder")
    data object Insurance : LifeVaultDestination("insurance")
    data object DigitalLegacy : LifeVaultDestination("digital_legacy")
    data object SecureVault : LifeVaultDestination("secure_vault")
    data object Sos : LifeVaultDestination("sos")
    data object Profile : LifeVaultDestination("profile")
    data object Alerts : LifeVaultDestination("alerts")
    data object AiAssistant : LifeVaultDestination("ai_assistant")
    data object QrCard : LifeVaultDestination("qr_card")
    data object Settings : LifeVaultDestination("settings")
    data object EditProfile : LifeVaultDestination("edit_profile")
    data object TrustedContacts : LifeVaultDestination("trusted_contacts")
    data object InfoPage : LifeVaultDestination("info_page/{pageKey}") {
        fun route(pageKey: String) = "info_page/$pageKey"
    }
    data object Feedback : LifeVaultDestination("feedback")
    data object PinSetup : LifeVaultDestination("pin_setup")
    data object PasswordVault : LifeVaultDestination("password_vault_screen")
}
