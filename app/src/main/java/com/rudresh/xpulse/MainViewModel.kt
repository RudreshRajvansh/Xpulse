package com.rudresh.xpulse

import androidx.lifecycle.ViewModel
import com.rudresh.xpulse.core.security.SecurityPrefs
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val securityPrefs: SecurityPrefs,
) : ViewModel() {
    val currentUser = sessionManager.currentUser
    val needsOnboarding = sessionManager.needsOnboarding
    val lockEnabled = securityPrefs.lockEnabled
    val unlocked = securityPrefs.unlocked

    fun markUnlocked() = securityPrefs.markUnlocked()

    fun logout() {
        securityPrefs.lock()
        sessionManager.endSession()
    }
}
