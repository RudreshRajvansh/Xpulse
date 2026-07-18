package com.rudresh.xpulse.feature.pharmacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.usecase.FulfillPrescriptionUseCase
import com.rudresh.xpulse.core.domain.usecase.GetPendingPrescriptionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PharmacyState(
    val loading: Boolean = false,
    val prescriptions: List<Prescription> = emptyList(),
    val fulfillingId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val getPendingPrescriptionsUseCase: GetPendingPrescriptionsUseCase,
    private val fulfillPrescriptionUseCase: FulfillPrescriptionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PharmacyState())
    val state: StateFlow<PharmacyState> = _state.asStateFlow()

    init {
        loadPending()
    }

    fun loadPending() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = getPendingPrescriptionsUseCase()) {
                is Result.Success -> _state.value = _state.value.copy(prescriptions = r.data, loading = false)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, loading = false)
            }
        }
    }

    fun fulfill(prescriptionId: String) {
        if (_state.value.fulfillingId != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(fulfillingId = prescriptionId)
            when (val r = fulfillPrescriptionUseCase(prescriptionId)) {
                is Result.Success -> _state.value = _state.value.copy(
                    fulfillingId = null,
                    prescriptions = _state.value.prescriptions.filterNot { it.id == prescriptionId },
                )
                is Result.Error -> _state.value = _state.value.copy(fulfillingId = null, error = r.message)
            }
        }
    }
}
