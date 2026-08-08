package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.repository.SupportRepository
import javax.inject.Inject

class GetUserTicketsUseCase @Inject constructor(
    private val supportRepository: SupportRepository,
) {
    suspend operator fun invoke(userId: String): Result<List<SupportTicket>> =
        supportRepository.getTicketsForUser(userId)
}
