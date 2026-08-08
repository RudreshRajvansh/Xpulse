package com.rudresh.xpulse.core.domain.model

enum class TicketStatus { OPEN, RESOLVED }

data class SupportTicket(
    val id: String,
    val raisedBy: String,
    val raisedByName: String,
    val subject: String,
    val detail: String,
    val status: TicketStatus,
    val createdAt: Long,
    val resolution: String?,
    val resolvedAt: Long?,
)
