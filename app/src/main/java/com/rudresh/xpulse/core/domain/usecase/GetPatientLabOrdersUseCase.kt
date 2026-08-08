package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.repository.LabRepository
import javax.inject.Inject

class GetPatientLabOrdersUseCase @Inject constructor(
    private val labRepository: LabRepository,
) {
    suspend operator fun invoke(patientId: String): Result<List<LabOrder>> =
        labRepository.getLabOrdersForPatient(patientId)
}
