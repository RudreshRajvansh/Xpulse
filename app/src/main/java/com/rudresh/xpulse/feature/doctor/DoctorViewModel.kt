package com.rudresh.xpulse.feature.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.usecase.GetActiveGrantsUseCase
import com.rudresh.xpulse.core.domain.usecase.IssuePrescriptionUseCase
import com.rudresh.xpulse.core.domain.usecase.OrderLabTestUseCase
import com.rudresh.xpulse.core.domain.usecase.VerifyAccessUseCase
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorState(
    val requestsLoading: Boolean = false,
    val requests: List<AccessGrant> = emptyList(),
    val openingGrantId: String? = null,
    val scoped: ScopedData? = null,
    val patientId: String? = null,
    val error: String? = null,
    val issuing: Boolean = false,
    val issuedMessage: String? = null,
    val orderingLab: Boolean = false,
    val labMessage: String? = null,
)

@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val getActiveGrantsUseCase: GetActiveGrantsUseCase,
    private val verifyAccessUseCase: VerifyAccessUseCase,
    private val issuePrescriptionUseCase: IssuePrescriptionUseCase,
    private val orderLabTestUseCase: OrderLabTestUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(DoctorState())
    val state: StateFlow<DoctorState> = _state.asStateFlow()

    private val doctorId: String get() = sessionManager.currentUser.value?.id.orEmpty()

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(requestsLoading = true)
            when (val r = getActiveGrantsUseCase(doctorId)) {
                is Result.Success -> _state.value = _state.value.copy(requests = r.data, requestsLoading = false)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, requestsLoading = false)
            }
        }
    }

    fun open(grant: AccessGrant) {
        viewModelScope.launch {
            _state.value = _state.value.copy(openingGrantId = grant.id, scoped = null, error = null, issuedMessage = null)
            when (val r = verifyAccessUseCase(grant.id)) {
                is Result.Success -> _state.value = _state.value.copy(scoped = r.data, patientId = grant.patientId, openingGrantId = null)
                is Result.Error -> _state.value = _state.value.copy(error = r.message, openingGrantId = null)
            }
        }
    }

    fun closePatient() {
        _state.value = _state.value.copy(
            scoped = null,
            patientId = null,
            error = null,
            issuedMessage = null,
            labMessage = null,
        )
        loadRequests()
    }

    fun orderLabTest(testName: String) {
        val pid = _state.value.patientId ?: return
        if (_state.value.orderingLab || testName.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(orderingLab = true, labMessage = null)
            when (val r = orderLabTestUseCase(pid, testName, doctorId)) {
                is Result.Success -> _state.value = _state.value.copy(
                    orderingLab = false,
                    labMessage = "${r.data.testName} sent to diagnostics",
                )
                is Result.Error -> _state.value = _state.value.copy(orderingLab = false, labMessage = r.message)
            }
        }
    }

    fun issuePrescription(name: String, dose: String, frequency: String) {
        val pid = _state.value.patientId ?: return
        if (_state.value.issuing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(issuing = true, issuedMessage = null)
            val item = Medicine(id = "m_${System.currentTimeMillis()}", name = name, dose = dose, frequency = frequency, isPrn = false)
            when (val r = issuePrescriptionUseCase(pid, doctorId, listOf(item))) {
                is Result.Success -> _state.value = _state.value.copy(
                    issuing = false,
                    issuedMessage = "Prescription sent",
                    scoped = _state.value.scoped?.copy(medicines = _state.value.scoped!!.medicines + item),
                )
                is Result.Error -> _state.value = _state.value.copy(issuing = false, issuedMessage = r.message)
            }
        }
    }
}
