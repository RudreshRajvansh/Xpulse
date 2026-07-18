package com.rudresh.xpulse.core.domain.model

data class Prescription(
    val id: String,
    val patientId: String,
    val doctorId: String,
    val items:List<Medicine>,
    val issuedAt: Long
)
