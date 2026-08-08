package com.rudresh.xpulse.feature.care

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.usecase.GetTicketsUseCase
import com.rudresh.xpulse.core.domain.usecase.ResolveTicketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerCareState(
    val loading: Boolean = false,
    val tickets: List<SupportTicket> = emptyList(),
    val resolvingId: String? = null,
    val busyId: String? = null,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class CustomerCareViewModel @Inject constructor(
    private val getTicketsUseCase: GetTicketsUseCase,
    private val resolveTicketUseCase: ResolveTicketUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerCareState())
    val state: StateFlow<CustomerCareState> = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = getTicketsUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(tickets = r.data, loading = false)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, loading = false)
            }
        }
    }

    fun openResolve(ticketId: String) {
        _state.value = _state.value.copy(resolvingId = ticketId, message = null)
    }

    fun cancelResolve() {
        _state.value = _state.value.copy(resolvingId = null)
    }

    fun resolve(ticketId: String, resolution: String) {
        if (resolution.isBlank() || _state.value.busyId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busyId = ticketId)
            when (val r = resolveTicketUseCase(ticketId, resolution)) {
                is Result.Success -> _state.value = _state.value.copy(
                    busyId = null,
                    resolvingId = null,
                    tickets = _state.value.tickets.map { if (it.id == ticketId) r.data else it },
                    message = "Ticket resolved for ${r.data.raisedByName}",
                )
                is Result.Error -> _state.value = _state.value.copy(busyId = null, error = r.message)
            }
        }
    }
}
