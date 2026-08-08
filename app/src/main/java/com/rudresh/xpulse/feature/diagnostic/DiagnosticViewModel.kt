package com.rudresh.xpulse.feature.diagnostic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.usecase.CollectSampleUseCase
import com.rudresh.xpulse.core.domain.usecase.CompleteLabOrderUseCase
import com.rudresh.xpulse.core.domain.usecase.GetLabOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticState(
    val loading: Boolean = false,
    val orders: List<LabOrder> = emptyList(),
    val busyId: String? = null,
    val publishingId: String? = null,
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class DiagnosticViewModel @Inject constructor(
    private val getLabOrdersUseCase: GetLabOrdersUseCase,
    private val collectSampleUseCase: CollectSampleUseCase,
    private val completeLabOrderUseCase: CompleteLabOrderUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DiagnosticState())
    val state: StateFlow<DiagnosticState> = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = getLabOrdersUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(orders = r.data, loading = false)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, loading = false)
            }
        }
    }

    fun collectSample(orderId: String) {
        if (_state.value.busyId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busyId = orderId, message = null)
            when (val r = collectSampleUseCase(orderId)) {
                is Result.Success -> _state.value = _state.value.copy(
                    busyId = null,
                    orders = _state.value.orders.map { if (it.id == orderId) r.data else it },
                    message = "Sample collected for ${r.data.testName}",
                )
                is Result.Error -> _state.value = _state.value.copy(busyId = null, error = r.message)
            }
        }
    }

    fun openPublish(orderId: String) {
        _state.value = _state.value.copy(publishingId = orderId, message = null)
    }

    fun cancelPublish() {
        _state.value = _state.value.copy(publishingId = null)
    }

    fun publishReport(orderId: String, resultSummary: String) {
        if (resultSummary.isBlank() || _state.value.busyId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(busyId = orderId, message = null)
            when (val r = completeLabOrderUseCase(orderId, resultSummary)) {
                is Result.Success -> _state.value = _state.value.copy(
                    busyId = null,
                    publishingId = null,
                    orders = _state.value.orders.map { if (it.id == orderId) r.data else it },
                    message = "Report published for ${r.data.testName}",
                )
                is Result.Error -> _state.value = _state.value.copy(busyId = null, error = r.message)
            }
        }
    }
}
