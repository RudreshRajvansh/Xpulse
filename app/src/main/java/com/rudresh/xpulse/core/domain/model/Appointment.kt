package com.rudresh.xpulse.core.domain.model

data class Appointment(
    val id: String,
    val patientId: String,
    val patientName: String,
    val checkedInAt: Long,
)
