package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Recommendation
import com.rudresh.xpulse.core.domain.repository.ProfileRepository
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(patientId: String): Result<List<Recommendation>> =
        profileRepository.getRecommendations(patientId)
}
