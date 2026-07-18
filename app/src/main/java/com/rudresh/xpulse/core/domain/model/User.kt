package com.rudresh.xpulse.core.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val roles: Set<Role>,
    val scopeId: String?
)
