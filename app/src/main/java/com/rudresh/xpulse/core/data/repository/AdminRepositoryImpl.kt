package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.core.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : AdminRepository {

    override suspend fun getPlatformStats(): Result<PlatformStats> =
        try {
            Result.Success(remote.getPlatformStats())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load platform stats", e)
        }

    override suspend fun getUsers(): Result<List<User>> =
        try {
            Result.Success(remote.getUsers())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load users", e)
        }

    override suspend fun createStaffAccount(name: String, email: String, password: String, role: Role, scopeId: String?): Result<User> =
        try {
            Result.Success(remote.createStaffAccount(name, email, password, role, scopeId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not create account", e)
        }
}
