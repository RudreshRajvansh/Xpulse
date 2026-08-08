package com.rudresh.xpulse.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.HttpClient
import com.rudresh.xpulse.core.data.remote.ServerConfig
import com.rudresh.xpulse.core.domain.usecase.LoginUseCase
import com.rudresh.xpulse.core.domain.usecase.RegisterUseCase
import com.rudresh.xpulse.core.domain.usecase.RequestPasswordResetUseCase
import com.rudresh.xpulse.core.domain.usecase.ResetPasswordUseCase
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

data class ResetState(
    val active: Boolean = false,
    val step: Int = 0,
    val email: String = "",
    val sentCode: String? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

enum class ServerStatus { CHECKING, ONLINE, OFFLINE }

data class ConnectionState(
    val status: ServerStatus = ServerStatus.CHECKING,
    val baseUrl: String = "",
    val editing: Boolean = false,
)

private val EMAIL_PATTERN = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$")

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val serverConfig: ServerConfig,
    private val httpClient: HttpClient,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _resetState = MutableStateFlow(ResetState())
    val resetState: StateFlow<ResetState> = _resetState.asStateFlow()

    private val _connection = MutableStateFlow(ConnectionState(baseUrl = serverConfig.baseUrl))
    val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    init {
        checkConnection()
    }

    fun checkConnection() {
        viewModelScope.launch {
            _connection.value = _connection.value.copy(
                status = ServerStatus.CHECKING,
                baseUrl = serverConfig.baseUrl,
            )
            val reachable = httpClient.isServerReachable()
            _connection.value = _connection.value.copy(
                status = if (reachable) ServerStatus.ONLINE else ServerStatus.OFFLINE,
                baseUrl = serverConfig.baseUrl,
            )
        }
    }

    fun toggleServerEditor() {
        _connection.value = _connection.value.copy(editing = !_connection.value.editing)
    }

    fun updateServerUrl(url: String) {
        serverConfig.baseUrl = url
        _connection.value = _connection.value.copy(baseUrl = serverConfig.baseUrl, editing = false)
        checkConnection()
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter your email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = loginUseCase(email.trim(), password)) {
                is Result.Success -> {
                    sessionManager.startSession(result.data)
                    _uiState.value = LoginUiState.Idle
                }
                is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean,
    ) {
        val error = validateRegistration(name, email, phone, password, confirmPassword, acceptedTerms)
        if (error != null) {
            _uiState.value = LoginUiState.Error(error)
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = registerUseCase(name.trim(), email.trim(), phone.trim(), password)) {
                is Result.Success -> {
                    sessionManager.startSession(result.data)
                    _uiState.value = LoginUiState.Idle
                }
                is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }

    private fun validateRegistration(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean,
    ): String? = when {
        name.isBlank() -> "Enter your full name"
        !EMAIL_PATTERN.matches(email.trim()) -> "Enter a valid email address"
        phone.trim().length != 10 || phone.trim().any { !it.isDigit() } -> "Enter a valid 10-digit mobile number"
        password.length < 8 -> "Password must be at least 8 characters"
        password.none { it.isDigit() } -> "Password must contain at least one number"
        password != confirmPassword -> "Passwords do not match"
        !acceptedTerms -> "Please accept the terms to continue"
        else -> null
    }

    fun startReset(email: String) {
        _uiState.value = LoginUiState.Idle
        _resetState.value = ResetState(active = true, email = email.trim())
    }

    fun cancelReset() {
        _resetState.value = ResetState()
    }

    fun updateResetEmail(email: String) {
        _resetState.value = _resetState.value.copy(email = email, error = null)
    }

    fun requestCode() {
        val email = _resetState.value.email.trim()
        if (!EMAIL_PATTERN.matches(email)) {
            _resetState.value = _resetState.value.copy(error = "Enter a valid email address")
            return
        }
        if (_resetState.value.busy) return
        viewModelScope.launch {
            _resetState.value = _resetState.value.copy(busy = true, error = null)
            when (val r = requestPasswordResetUseCase(email)) {
                is Result.Success -> _resetState.value = _resetState.value.copy(busy = false, step = 1, sentCode = r.data)
                is Result.Error -> _resetState.value = _resetState.value.copy(busy = false, error = r.message)
            }
        }
    }

    fun submitNewPassword(otp: String, newPassword: String, confirmPassword: String) {
        val current = _resetState.value
        val error = when {
            otp.isBlank() -> "Enter the code we sent you"
            newPassword.length < 8 -> "Password must be at least 8 characters"
            newPassword.none { it.isDigit() } -> "Password must contain at least one number"
            newPassword != confirmPassword -> "Passwords do not match"
            else -> null
        }
        if (error != null) {
            _resetState.value = current.copy(error = error)
            return
        }
        if (current.busy) return
        viewModelScope.launch {
            _resetState.value = current.copy(busy = true, error = null)
            when (val r = resetPasswordUseCase(current.email.trim(), otp, newPassword)) {
                is Result.Success -> {
                    _resetState.value = ResetState()
                    _uiState.value = LoginUiState.Info("Password updated. Sign in with your new password.")
                }
                is Result.Error -> _resetState.value = _resetState.value.copy(busy = false, error = r.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = LoginUiState.Idle
    }
}
