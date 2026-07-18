package com.rudresh.xpulse.feature.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.usecase.VerifyAccessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorState(
    val loading: Boolean = false,
    val scoped: ScopedData? = null,
    val error: String? = null,
)

@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val verifyAccess: VerifyAccessUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DoctorState())
    val state: StateFlow<DoctorState> = _state.asStateFlow()

    fun verify(grantId: String) {
        viewModelScope.launch {
            _state.value = DoctorState(loading = true)
            when (val r = verifyAccess(grantId.trim())) {
                is Result.Success -> _state.value = DoctorState(scoped = r.data)
                is Result.Error -> _state.value = DoctorState(error = r.message)
            }
        }
    }
}
