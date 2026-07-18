package com.rudresh.xpulse.feature.reception

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.usecase.AdmitUseCase
import com.rudresh.xpulse.core.domain.usecase.GetQueueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceptionState(
    val queue: List<Appointment> = emptyList(),
    val queueLoading: Boolean = false,
    val admittingId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ReceptionViewModel @Inject constructor(
    private val getQueueUseCase: GetQueueUseCase,
    private val admitUseCase: AdmitUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ReceptionState())
    val state: StateFlow<ReceptionState> = _state.asStateFlow()

    init {
        loadQueue()
    }

    fun loadQueue() {
        viewModelScope.launch {
            _state.value = _state.value.copy(queueLoading = true, error = null)
            when (val r = getQueueUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(queue = r.data, queueLoading = false)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, queueLoading = false)
            }
        }
    }

    fun admit(appointmentId: String) {
        if (_state.value.admittingId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(admittingId = appointmentId)
            when (val r = admitUseCase(appointmentId)) {
                is Result.Success -> _state.value = _state.value.copy(
                    admittingId = null,
                    queue = _state.value.queue.filterNot { it.id == appointmentId },
                )
                is Result.Error -> _state.value = _state.value.copy(admittingId = null, error = r.message)
            }
        }
    }
}
