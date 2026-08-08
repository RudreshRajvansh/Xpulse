package com.rudresh.xpulse.core.domain.model

data class MedicalReport(
    val id: String,
    val patientId: String,
    val category: String,
    val label: String,
    val uri: String,
    val addedAt: Long,
    val fileBytes: Int = 0,
    val stored: Boolean = false,
)
