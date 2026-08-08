package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.repository.ProfileRepository
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(patientId: String): Result<List<MedicalReport>> =
        profileRepository.getReports(patientId)
}
