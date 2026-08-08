package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.User

interface AdminRepository {
    suspend fun getPlatformStats(): Result<PlatformStats>
    suspend fun getUsers(): Result<List<User>>
    suspend fun createStaffAccount(name: String, email: String, password: String, role: Role, scopeId: String?): Result<User>
}
