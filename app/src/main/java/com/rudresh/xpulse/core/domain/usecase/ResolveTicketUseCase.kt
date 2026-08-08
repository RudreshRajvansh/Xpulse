package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.repository.SupportRepository
import javax.inject.Inject

class ResolveTicketUseCase @Inject constructor(
    private val supportRepository: SupportRepository,
) {
    suspend operator fun invoke(ticketId: String, resolution: String): Result<SupportTicket> =
        supportRepository.resolveTicket(ticketId, resolution)
}
