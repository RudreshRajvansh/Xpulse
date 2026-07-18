package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.ScopedData

interface AccessRepository {
    suspend fun grantAccess(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): Result<AccessGrant>
    suspend fun revoke(grantId: String): Result<AccessGrant>
    suspend fun verifyGrant(grantId: String): Result<ScopedData>
    suspend fun readAuditLog(patientId: String): Result<List<AuditEntry>>
}
