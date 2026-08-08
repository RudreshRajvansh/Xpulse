package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.core.domain.repository.AdminRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(): Result<List<User>> = adminRepository.getUsers()
}
