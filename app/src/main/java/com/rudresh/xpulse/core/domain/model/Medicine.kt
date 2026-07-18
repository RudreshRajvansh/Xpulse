package com.rudresh.xpulse.core.domain.model

data class Medicine(
    val id: String,
    val name: String,
    val dose: String,
    val frequency: String,
    val isPrn: Boolean,
)
