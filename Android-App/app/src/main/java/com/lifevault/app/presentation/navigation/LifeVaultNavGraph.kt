package com.lifevault.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lifevault.app.presentation.aiassistant.AiAssistantScreen
import com.lifevault.app.presentation.alerts.AlertsScreen
import com.lifevault.app.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.lifevault.app.presentation.auth.login.LoginScreen
import com.lifevault.app.presentation.auth.register.RegisterScreen
import com.lifevault.app.presentation.dashboard.DashboardScreen
import com.lifevault.app.presentation.digitallegacy.DigitalLegacyScreen
import com.lifevault.app.presentation.documents.DocumentsScreen
import com.lifevault.app.presentation.editprofile.EditProfileScreen
import com.lifevault.app.presentation.emergencyprofile.EmergencyProfileScreen
import com.lifevault.app.presentation.feedback.FeedbackScreen
import com.lifevault.app.presentation.insurance.InsuranceScreen
import com.lifevault.app.presentation.medicalrecords.MedicalRecordsScreen
import com.lifevault.app.presentation.medicinereminder.MedicineReminderScreen
import com.lifevault.app.presentation.nominee.AddNomineeScreen
import com.lifevault.app.presentation.nominee.NomineeDashboardScreen
import com.lifevault.app.presentation.nominee.NomineeLoginScreen
import com.lifevault.app.presentation.onboarding.OnboardingScreen
import com.lifevault.app.presentation.passwordvault.PasswordVaultScreen
import com.lifevault.app.presentation.pinlock.PinSetupScreen
import com.lifevault.app.presentation.profile.ProfileScreen
import com.lifevault.app.presentation.qrcard.QrCardScreen
import com.lifevault.app.presentation.securevault.SecureVaultScreen
import com.lifevault.app.presentation.settings.InfoPageScreen
import com.lifevault.app.presentation.settings.SettingsScreen
import com.lifevault.app.presentation.sos.SosScreen
import com.lifevault.app.presentation.splash.SplashScreen
import com.lifevault.app.presentation.trustedcontacts.TrustedContactsScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun LifeVaultNavGraph(
    navController: NavHostController,
    startDestination: String = LifeVaultDestination.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(LifeVaultDestination.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(LifeVaultDestination.Onboarding.route) {
                        popUpTo(LifeVaultDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Onboarding.route) {
            OnboardingScreen(
                onSkip = {
                    navController.navigate(LifeVaultDestination.Login.route) {
                        popUpTo(LifeVaultDestination.Onboarding.route) { inclusive = true }
                    }
                },
                onGetStarted = {
                    navController.navigate(LifeVaultDestination.Login.route) {
                        popUpTo(LifeVaultDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegistrationSuccess = {
                    navController.navigate(LifeVaultDestination.Dashboard.route) {
                        popUpTo(LifeVaultDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(LifeVaultDestination.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(LifeVaultDestination.ForgotPassword.route) },
                onNavigateToNomineeLogin = { navController.navigate(LifeVaultDestination.NomineeLogin.route) },
                onLoginSuccess = {
                    navController.navigate(LifeVaultDestination.Dashboard.route) {
                        popUpTo(LifeVaultDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(LifeVaultDestination.NomineeLogin.route) {
            NomineeLoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(LifeVaultDestination.NomineeDashboard.route) {
                        popUpTo(LifeVaultDestination.NomineeLogin.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.NomineeDashboard.route) {
            NomineeDashboardScreen(
                onLogout = {
                    navController.navigate(LifeVaultDestination.Login.route) {
                        popUpTo(LifeVaultDestination.NomineeDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Dashboard.route) {
            DashboardScreen(
                onQuickAccessClick = { itemId ->
                    when (itemId) {
                        "emergency_profile" -> navController.navigate(LifeVaultDestination.EmergencyProfile.route)
                        "medical_records" -> navController.navigate(LifeVaultDestination.MedicalRecords.route)
                        "important_documents" -> navController.navigate(LifeVaultDestination.Documents.route)
                        "nominee_management" -> navController.navigate(LifeVaultDestination.AddNominee.route)
                        "medicine_reminder" -> navController.navigate(LifeVaultDestination.MedicineReminder.route)
                        "insurance" -> navController.navigate(LifeVaultDestination.Insurance.route)
                        "digital_legacy" -> navController.navigate(LifeVaultDestination.DigitalLegacy.route)
                        "password_vault" -> navController.navigate(LifeVaultDestination.PasswordVault.route)
                        "storage_summary" -> navController.navigate(LifeVaultDestination.SecureVault.route)
                        "ai_assistant" -> navController.navigate(LifeVaultDestination.AiAssistant.route)
                        "qr_emergency_card" -> navController.navigate(LifeVaultDestination.QrCard.route)
                        "trusted_contacts" -> navController.navigate(LifeVaultDestination.TrustedContacts.route)
                        "settings" -> navController.navigate(LifeVaultDestination.Settings.route)
                        else -> { /* later phase */ }
                    }
                },
                onAddNomineeClick = { navController.navigate(LifeVaultDestination.AddNominee.route) },
                onSosClick = { navController.navigate(LifeVaultDestination.Sos.route) },
                onAlertsClick = { navController.navigate(LifeVaultDestination.Alerts.route) },
                onProfileClick = { navController.navigate(LifeVaultDestination.Profile.route) }
            )
        }

        composable(LifeVaultDestination.Sos.route) {
            SosScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.Profile.route) {
            ProfileScreen(
                onLoggedOut = {
                    navController.navigate(LifeVaultDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Alerts.route) {
            AlertsScreen()
        }

        composable(LifeVaultDestination.AiAssistant.route) {
            AiAssistantScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.AddNominee.route) {
            AddNomineeScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { navController.popBackStack() }
            )
        }

        composable(LifeVaultDestination.EmergencyProfile.route) {
            EmergencyProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.MedicalRecords.route) {
            MedicalRecordsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.Documents.route) {
            DocumentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.MedicineReminder.route) {
            MedicineReminderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.Insurance.route) {
            InsuranceScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.DigitalLegacy.route) {
            DigitalLegacyScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.SecureVault.route) {
            SecureVaultScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.QrCard.route) {
            QrCardScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.TrustedContacts.route) {
            TrustedContactsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(LifeVaultDestination.EditProfile.route) },
                onInfoPageClick = { pageKey ->
                    if (pageKey == "feedback") {
                        navController.navigate(LifeVaultDestination.Feedback.route)
                    } else {
                        navController.navigate(LifeVaultDestination.InfoPage.route(pageKey))
                    }
                },
                onPinLockClick = { navController.navigate(LifeVaultDestination.PinSetup.route) },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(LifeVaultDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(LifeVaultDestination.Feedback.route) {
            FeedbackScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.PinSetup.route) {
            PinSetupScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(LifeVaultDestination.PasswordVault.route) {
            PasswordVaultScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = LifeVaultDestination.InfoPage.route,
            arguments = listOf(navArgument("pageKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val pageKey = backStackEntry.arguments?.getString("pageKey") ?: "about"
            InfoPageScreen(pageKey = pageKey, onNavigateBack = { navController.popBackStack() })
        }
    }
}
