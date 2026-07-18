package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import javax.inject.Inject

class AccessRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : AccessRepository {

    override suspend fun grantAccess(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): Result<AccessGrant> =
        try {
            Result.Success(remote.grantAccess(patientId, granteeId, scope, expiresAt))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not grant access", e)
        }

    override suspend fun revoke(grantId: String): Result<AccessGrant> =
        try {
            Result.Success(remote.revoke(grantId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not revoke access", e)
        }

    override suspend fun verifyGrant(grantId: String): Result<ScopedData> =
        try {
            Result.Success(remote.verifyGrant(grantId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Access denied", e)
        }

    override suspend fun readAuditLog(patientId: String): Result<List<AuditEntry>> =
        try {
            Result.Success(remote.readAuditLog(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load audit log", e)
        }

    override suspend fun getActiveGrants(granteeId: String): Result<List<AccessGrant>> =
        try {
            Result.Success(remote.getActiveGrants(granteeId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load access requests", e)
        }
}
