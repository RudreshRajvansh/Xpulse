package com.rudresh.xpulse.core.domain.model

data class PatientProfile(
    val age: String = "",
    val city: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val conditions: Set<String> = emptySet(),
    val abhaConnected: Boolean = false,
    val insuranceConnected: Boolean = false,
    val onboarded: Boolean = false,
)
