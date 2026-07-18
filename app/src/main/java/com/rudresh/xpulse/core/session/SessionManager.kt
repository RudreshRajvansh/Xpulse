package com.rudresh.xpulse.core.session

import com.rudresh.xpulse.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _needsOnboarding = MutableStateFlow(false)
    val needsOnboarding: StateFlow<Boolean> = _needsOnboarding.asStateFlow()

    private val onboardedUserIds = mutableSetOf("u_patient")

    fun startSession(user: User) {
        _currentUser.value = user
        _needsOnboarding.value = user.id !in onboardedUserIds
    }

    fun completeOnboarding() {
        _currentUser.value?.let { onboardedUserIds.add(it.id) }
        _needsOnboarding.value = false
    }

    fun endSession() {
        _currentUser.value = null
        _needsOnboarding.value = false
    }
}
