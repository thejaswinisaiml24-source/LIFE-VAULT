package com.lifevault.app.domain.model

data class LifeVaultUser(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String? = null,
    val createdAtEpochMillis: Long = 0L
)
