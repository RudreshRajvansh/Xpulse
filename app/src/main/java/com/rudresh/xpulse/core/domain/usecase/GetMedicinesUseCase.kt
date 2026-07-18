package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import javax.inject.Inject

class GetMedicinesUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
) {
    suspend operator fun invoke(patientId: String): Result<List<Medicine>> =
        recordRepository.getMedicines(patientId)
}
