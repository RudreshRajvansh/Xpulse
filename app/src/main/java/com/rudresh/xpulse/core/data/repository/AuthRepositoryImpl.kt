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

    override suspend fun register(name: String, email: String, phone: String, password: String): Result<User> =
        try {
            Result.Success(remote.register(name, email, phone, password))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Registration failed", e)
        }

    override suspend fun requestPasswordReset(email: String): Result<String> =
        try {
            Result.Success(remote.requestPasswordReset(email))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not send reset code", e)
        }

    override suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> =
        try {
            Result.Success(remote.resetPassword(email, otp, newPassword))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not reset password", e)
        }

    override suspend fun logout() {
        remote.logout()
    }
}
