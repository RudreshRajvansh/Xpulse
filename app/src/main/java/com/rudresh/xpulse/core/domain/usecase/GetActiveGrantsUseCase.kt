package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class GetActiveGrantsUseCase @Inject constructor(
    private val accessRepository: AccessRepository,
) {
    suspend operator fun invoke(granteeId: String): Result<List<AccessGrant>> =
        accessRepository.getActiveGrants(granteeId)
}
