package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.SupportTicket

interface SupportRepository {
    suspend fun raiseTicket(userId: String, subject: String, detail: String): Result<SupportTicket>
    suspend fun getTickets(): Result<List<SupportTicket>>
    suspend fun getTicketsForUser(userId: String): Result<List<SupportTicket>>
    suspend fun resolveTicket(ticketId: String, resolution: String): Result<SupportTicket>
}
