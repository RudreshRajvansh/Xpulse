package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import javax.inject.Inject

class GetPendingPrescriptionsUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    suspend operator fun invoke(): Result<List<Prescription>> =
        recordRepository.getPendingPrescriptions()
}
