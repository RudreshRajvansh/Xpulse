package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.repository.AdminRepository
import javax.inject.Inject

class GetPlatformStatsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(): Result<PlatformStats> = adminRepository.getPlatformStats()
}
