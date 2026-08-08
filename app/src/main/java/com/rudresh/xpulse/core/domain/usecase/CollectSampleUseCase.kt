package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.repository.LabRepository
import javax.inject.Inject

class CollectSampleUseCase @Inject constructor(
    private val labRepository: LabRepository,
) {
    suspend operator fun invoke(orderId: String): Result<LabOrder> = labRepository.collectSample(orderId)
}
