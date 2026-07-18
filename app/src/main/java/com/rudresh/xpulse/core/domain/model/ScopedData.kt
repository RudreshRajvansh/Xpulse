package com.rudresh.xpulse.core.domain.model

data class ScopedData(
    val patientName: String,
    val medicines: List<Medicine>,
    val allergies: List<String>,
)
