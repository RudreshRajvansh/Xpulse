package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import javax.inject.Inject

class FulfillPrescriptionUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    suspend operator fun invoke(prescriptionId: String): Result<Prescription> =
        recordRepository.fulfillPrescription(prescriptionId)
}
