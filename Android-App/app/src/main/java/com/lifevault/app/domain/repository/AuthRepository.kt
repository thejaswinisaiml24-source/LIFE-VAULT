package com.lifevault.app.domain.repository

import com.lifevault.app.domain.model.LifeVaultUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun currentUser(): Flow<LifeVaultUser?>

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<LifeVaultUser>

    suspend fun login(emailOrPhone: String, password: String): Result<LifeVaultUser>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<LifeVaultUser>

    suspend fun signOut()
}
