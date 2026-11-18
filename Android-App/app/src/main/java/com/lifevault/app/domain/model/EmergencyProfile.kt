package com.lifevault.app.domain.model

/**
 * Stored at users/{uid}/emergencyProfile/profile — a single document per
 * user (not a collection), since a person has exactly one emergency profile.
 */
data class EmergencyProfile(
    val bloodGroup: String = "",
    val age: String = "",
    val gender: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val isOrganDonor: Boolean = false,
    val allergies: String = "",
    val medicalConditions: String = "",
    val currentMedicines: String = "",
    val emergencyNotes: String = "",
    val updatedAtEpochMillis: Long = 0L
)
