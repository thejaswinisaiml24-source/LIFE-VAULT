package com.lifevault.app.presentation.common

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed class BiometricAvailability {
    data object Available : BiometricAvailability()
    data object NoHardware : BiometricAvailability()
    data object NoneEnrolled : BiometricAvailability()
    data object Unavailable : BiometricAvailability()
}

fun checkBiometricAvailability(activity: FragmentActivity): BiometricAvailability {
    val manager = BiometricManager.from(activity)
    return when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
        else -> BiometricAvailability.Unavailable
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String = "Unlock LifeVault",
    subtitle: String = "Use your fingerprint or face to continue",
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError(errString.toString())
        }

        override fun onAuthenticationFailed() {
            onError("Fingerprint/face not recognized. Try again.")
        }
    }

    val prompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    prompt.authenticate(promptInfo)
}
