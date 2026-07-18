package com.rudresh.xpulse.core.domain.model

data class AuditEntry(
    val id: String,
    val actor: String,
    val action: String,
    val subjectId: String,
    val timestamp: Long,
)
