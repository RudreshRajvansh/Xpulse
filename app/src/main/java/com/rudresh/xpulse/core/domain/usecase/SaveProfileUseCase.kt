package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.repository.ProfileRepository
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(patientId: String, profile: PatientProfile): Result<PatientProfile> =
        profileRepository.saveProfile(patientId, profile)
}
