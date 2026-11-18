package com.lifevault.app.data.util

import java.security.MessageDigest

/**
 * SHA-256 password hashing for nominee login credentials (nominees don't
 * have Firebase Auth accounts — they authenticate against a Firestore-side
 * credential doc). Same approach used in the earlier prototype's nominee
 * system. Not a substitute for bcrypt/Argon2 in a real production system,
 * but real hashing (not plaintext) and consistent with what's already here.
 */
object PasswordHasher {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
