package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class GetAuditLogUseCase @Inject constructor(
    private val accessRepository: AccessRepository,
) {
    suspend operator fun invoke(patientId: String): Result<List<AuditEntry>> =
        accessRepository.readAuditLog(patientId)
}
