package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import javax.inject.Inject

class IssuePrescriptionUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    suspend operator fun invoke(patientId: String, doctorId: String, items: List<Medicine>): Result<Prescription> =
        recordRepository.issuePrescription(patientId, doctorId, items)
}
