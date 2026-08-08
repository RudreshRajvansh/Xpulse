package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.repository.LabRepository
import javax.inject.Inject

class OrderLabTestUseCase @Inject constructor(
    private val labRepository: LabRepository,
) {
    suspend operator fun invoke(patientId: String, testName: String, orderedBy: String): Result<LabOrder> =
        labRepository.orderLabTest(patientId, testName, orderedBy)
}
