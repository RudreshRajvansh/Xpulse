package com.rudresh.xpulse.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.User
import com.rudresh.xpulse.core.domain.usecase.CreateStaffAccountUseCase
import com.rudresh.xpulse.core.domain.usecase.GetPlatformStatsUseCase
import com.rudresh.xpulse.core.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val loading: Boolean = false,
    val stats: PlatformStats? = null,
    val users: List<User> = emptyList(),
    val creating: Boolean = false,
    val createMessage: String? = null,
    val createError: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getPlatformStatsUseCase: GetPlatformStatsUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val createStaffAccountUseCase: CreateStaffAccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val stats = getPlatformStatsUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(stats = stats.data)
                is Result.Error -> _state.value = _state.value.copy(error = stats.message)
            }
            when (val users = getUsersUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(users = users.data, loading = false)
                is Result.Error -> _state.value = _state.value.copy(error = users.message, loading = false)
            }
        }
    }

    fun createStaff(name: String, email: String, password: String, role: Role, scopeId: String?) {
        if (_state.value.creating) return
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, createMessage = null, createError = null)
            when (val r = createStaffAccountUseCase(name, email, password, role, scopeId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        creating = false,
                        createMessage = "${r.data.name} can now sign in with ${r.data.email}",
                    )
                    refresh()
                }
                is Result.Error -> _state.value = _state.value.copy(creating = false, createError = r.message)
            }
        }
    }

    fun clearCreateMessage() {
        _state.value = _state.value.copy(createMessage = null, createError = null)
    }
}
