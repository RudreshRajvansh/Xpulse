package com.rudresh.xpulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.domain.usecase.LogoutUseCase
import com.rudresh.xpulse.core.security.SecurityPrefs
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val securityPrefs: SecurityPrefs,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val currentUser = sessionManager.currentUser
    val needsOnboarding = sessionManager.needsOnboarding
    val lockEnabled = securityPrefs.lockEnabled
    val unlocked = securityPrefs.unlocked

    fun markUnlocked() = securityPrefs.markUnlocked()

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
        securityPrefs.lock()
        sessionManager.endSession()
    }
}
