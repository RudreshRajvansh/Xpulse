package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.core.domain.repository.AdminRepository
import javax.inject.Inject

class CreateStaffAccountUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String, role: Role, scopeId: String?): Result<User> =
        adminRepository.createStaffAccount(name, email, password, role, scopeId)
}
