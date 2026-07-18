package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class GrantAccessUseCase @Inject constructor(
    private val accessRepository: AccessRepository,
) {
    suspend operator fun invoke(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): Result<AccessGrant> =
        accessRepository.grantAccess(patientId, granteeId, scope, expiresAt)
}
