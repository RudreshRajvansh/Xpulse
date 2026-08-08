package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.repository.SupportRepository
import javax.inject.Inject

class RaiseTicketUseCase @Inject constructor(
    private val supportRepository: SupportRepository,
) {
    suspend operator fun invoke(userId: String, subject: String, detail: String): Result<SupportTicket> =
        supportRepository.raiseTicket(userId, subject, detail)
}
