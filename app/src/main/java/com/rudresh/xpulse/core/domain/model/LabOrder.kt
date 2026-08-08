package com.rudresh.xpulse.core.domain.model

enum class LabOrderStatus { ORDERED, SAMPLE_COLLECTED, COMPLETED }

data class LabOrder(
    val id: String,
    val patientId: String,
    val patientName: String,
    val testName: String,
    val orderedBy: String,
    val orderedAt: Long,
    val status: LabOrderStatus,
    val resultSummary: String?,
    val completedAt: Long?,
)
