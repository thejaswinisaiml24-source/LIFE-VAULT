package com.lifevault.app.data.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Real AES-256-GCM encryption for Password Vault entries. The encryption
 * key is derived from the user's VAULT MASTER PASSWORD (separate from their
 * account login password) via PBKDF2 — NOT the Android Keystore, because a
 * Keystore-bound key can't be used to decrypt on a different device, and
 * vault entries need to be readable wherever the user logs in.
 *
 * HONEST LIMITATION: if the master password is forgotten, entries CANNOT
 * be recovered — there is no backdoor, by design (that's what "real
 * encryption" means). This is stated clearly in the Vault UI.
 */
object VaultCrypto {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun deriveKey(masterPassword: String, saltBase64: String): SecretKeySpec {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /** Returns "ivBase64:cipherTextBase64" */
    fun encrypt(plainText: String, masterPassword: String, saltBase64: String): String {
        val key = deriveKey(masterPassword, saltBase64)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(cipherText, Base64.NO_WRAP)}"
    }

    /** Returns null if the master password is wrong (auth tag check fails). */
    fun decrypt(payload: String, masterPassword: String, saltBase64: String): String? {
        return try {
            val parts = payload.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            val key = deriveKey(masterPassword, saltBase64)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
