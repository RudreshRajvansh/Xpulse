package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.repository.SupportRepository
import javax.inject.Inject

class SupportRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : SupportRepository {

    override suspend fun raiseTicket(userId: String, subject: String, detail: String): Result<SupportTicket> =
        try {
            Result.Success(remote.raiseTicket(userId, subject, detail))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not raise ticket", e)
        }

    override suspend fun getTickets(): Result<List<SupportTicket>> =
        try {
            Result.Success(remote.getTickets())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load tickets", e)
        }

    override suspend fun getTicketsForUser(userId: String): Result<List<SupportTicket>> =
        try {
            Result.Success(remote.getTicketsForUser(userId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load your tickets", e)
        }

    override suspend fun resolveTicket(ticketId: String, resolution: String): Result<SupportTicket> =
        try {
            Result.Success(remote.resolveTicket(ticketId, resolution))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not resolve ticket", e)
        }
}
