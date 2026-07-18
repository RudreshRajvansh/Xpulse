package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class VerifyAccessUseCase @Inject constructor(
    private val accessRepository: AccessRepository,
) {
    suspend operator fun invoke(grantId: String): Result<ScopedData> =
        accessRepository.verifyGrant(grantId)
}
