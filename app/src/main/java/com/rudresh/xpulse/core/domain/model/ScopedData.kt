package com.rudresh.xpulse.core.domain.model

data class ScopedData(
    val patientName: String,
    val medicines: List<Medicine>,
    val allergies: List<String>,
    val profile: PatientProfile = PatientProfile(),
    val labOrders: List<LabOrder> = emptyList(),
    val reports: List<MedicalReport> = emptyList(),
)
