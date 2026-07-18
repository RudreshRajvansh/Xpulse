package com.rudresh.xpulse.feature.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.usecase.GetAuditLogUseCase
import com.rudresh.xpulse.core.domain.usecase.GetMedicinesUseCase
import com.rudresh.xpulse.core.domain.usecase.GrantAccessUseCase
import com.rudresh.xpulse.core.domain.usecase.RevokeAccessUseCase
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientState(
    val medicines: List<Medicine> = emptyList(),
    val medicinesLoading: Boolean = false,
    val grantId: String? = null,
    val grantRevoked: Boolean = false,
    val grantBusy: Boolean = false,
    val audit: List<AuditEntry> = emptyList(),
    val auditLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val getMedicines: GetMedicinesUseCase,
    private val grantAccess: GrantAccessUseCase,
    private val revokeAccess: RevokeAccessUseCase,
    private val getAuditLog: GetAuditLogUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PatientState())
    val state: StateFlow<PatientState> = _state.asStateFlow()

    private val patientId: String get() = sessionManager.currentUser.value?.id.orEmpty()

    init {
        loadMedicines()
    }

    fun loadMedicines() {
        viewModelScope.launch {
            _state.value = _state.value.copy(medicinesLoading = true)
            when (val r = getMedicines(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(medicines = r.data, medicinesLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, medicinesLoading = false)
            }
        }
    }

    fun grant() {
        if (_state.value.grantBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(grantBusy = true)
            val expiresAt = System.currentTimeMillis() + 120_000
            when (val r = grantAccess(patientId, "u_doctor", setOf("Medicines", "Allergies"), expiresAt)) {
                is Result.Success -> _state.value = _state.value.copy(grantId = r.data.id, grantRevoked = false, message = "Access granted", grantBusy = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, grantBusy = false)
            }
            loadAudit()
        }
    }

    fun revoke() {
        val id = _state.value.grantId ?: return
        if (_state.value.grantBusy) return
        viewModelScope.launch {
            _state.value = _state.value.copy(grantBusy = true)
            when (val r = revokeAccess(id)) {
                is Result.Success -> _state.value = _state.value.copy(grantRevoked = true, message = "Access revoked", grantBusy = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, grantBusy = false)
            }
            loadAudit()
        }
    }

    fun loadAudit() {
        viewModelScope.launch {
            _state.value = _state.value.copy(auditLoading = true)
            when (val r = getAuditLog(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(audit = r.data, auditLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, auditLoading = false)
            }
        }
    }
}
