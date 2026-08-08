package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, phone: String, password: String): Result<User>
    suspend fun requestPasswordReset(email: String): Result<String>
    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit>
    suspend fun logout()
}
