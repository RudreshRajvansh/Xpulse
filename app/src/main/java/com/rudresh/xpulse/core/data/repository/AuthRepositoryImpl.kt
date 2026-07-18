package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.core.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        try {
            Result.Success(remote.login(email, password))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Login failed", e)
        }

    override suspend fun register(name: String, email: String, password: String): Result<User> =
        try {
            Result.Success(remote.register(name, email, password))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed", e)
        }

    override suspend fun logout() {
        remote.logout()
    }
}
