package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class RevokeAccessUseCase @Inject constructor(
    private val accessRepository: AccessRepository,
) {
    suspend operator fun invoke(grantId: String): Result<AccessGrant> =
        accessRepository.revoke(grantId)
}
