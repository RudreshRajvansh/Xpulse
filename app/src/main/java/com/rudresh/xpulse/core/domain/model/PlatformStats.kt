package com.rudresh.xpulse.core.domain.model

data class PlatformStats(
    val totalUsers: Int,
    val patients: Int,
    val staff: Int,
    val prescriptionsIssued: Int,
    val prescriptionsPending: Int,
    val labOrdersOpen: Int,
    val labOrdersCompleted: Int,
    val activeGrants: Int,
    val revokedGrants: Int,
    val waitingInQueue: Int,
    val openTickets: Int,
)
