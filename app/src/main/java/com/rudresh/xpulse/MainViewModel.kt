package com.rudresh.xpulse

import androidx.lifecycle.ViewModel
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
) : ViewModel() {
    val currentUser = sessionManager.currentUser
    val needsOnboarding = sessionManager.needsOnboarding
    fun logout() = sessionManager.endSession()
}
