package com.rudresh.xpulse.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.usecase.LoginUseCase
import com.rudresh.xpulse.core.domain.usecase.RegisterUseCase
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
    data class Info(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = loginUseCase(email, password)) {
                is Result.Success -> {
                    sessionManager.startSession(result.data)
                    _uiState.value = LoginUiState.Idle
                }
                is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = registerUseCase(name, email, password)) {
                is Result.Success -> {
                    sessionManager.startSession(result.data)
                    _uiState.value = LoginUiState.Idle
                }
                is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter your email first")
            return
        }
        _uiState.value = LoginUiState.Info("If $email is registered, a reset link has been sent.")
    }

    fun clearMessage() {
        _uiState.value = LoginUiState.Idle
    }
}
