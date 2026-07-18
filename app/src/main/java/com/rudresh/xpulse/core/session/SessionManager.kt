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

    fun startSession(user: User) {
        _currentUser.value = user
    }

    fun endSession() {
        _currentUser.value = null
    }
}
