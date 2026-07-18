package com.rudresh.xpulse.core.domain.model

data class AccessGrant(
    val id: String,
    val patientId: String,
    val granteeId: String,
    val scope: Set<String>,
    val issuedAt: Long,
    val expiresAt: Long,
    val revoked: Boolean,
)
