package com.lifevault.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.lifevault.app.di.IoDispatcher
import com.lifevault.app.domain.model.LifeVaultUser
import com.lifevault.app.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS_COLLECTION = "users"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthRepository {

    override fun currentUser(): Flow<LifeVaultUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection(USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(LifeVaultUser::class.java)
                        trySend(user)
                    }
                    .addOnFailureListener { trySend(null) }
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<LifeVaultUser> = withContext(ioDispatcher) {
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: return@withContext Result.failure(IllegalStateException("Registration failed: no UID returned"))

            val user = LifeVaultUser(
                uid = uid,
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                profilePhotoUrl = null,
                createdAtEpochMillis = System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(emailOrPhone: String, password: String): Result<LifeVaultUser> =
        withContext(ioDispatcher) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(emailOrPhone, password).await()
                val uid = authResult.user?.uid
                    ?: return@withContext Result.failure(IllegalStateException("Login failed: no UID returned"))

                val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
                val user = doc.toObject(LifeVaultUser::class.java)
                    ?: return@withContext Result.failure(IllegalStateException("User profile not found"))

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<LifeVaultUser> =
        withContext(ioDispatcher) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                    ?: return@withContext Result.failure(IllegalStateException("Google sign-in failed: no user returned"))

                val userDocRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
                val existingDoc = userDocRef.get().await()

                val user = if (existingDoc.exists()) {
                    existingDoc.toObject(LifeVaultUser::class.java)
                        ?: return@withContext Result.failure(IllegalStateException("Existing profile is corrupted"))
                } else {
                    val newUser = LifeVaultUser(
                        uid = firebaseUser.uid,
                        fullName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        phoneNumber = firebaseUser.phoneNumber ?: "",
                        profilePhotoUrl = firebaseUser.photoUrl?.toString(),
                        createdAtEpochMillis = System.currentTimeMillis()
                    )
                    userDocRef.set(newUser).await()
                    newUser
                }

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
