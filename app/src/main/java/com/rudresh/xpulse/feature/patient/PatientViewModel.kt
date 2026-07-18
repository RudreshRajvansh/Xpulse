package com.rudresh.xpulse.feature.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.usecase.CheckInUseCase
import com.rudresh.xpulse.core.domain.usecase.GetAuditLogUseCase
import com.rudresh.xpulse.core.domain.usecase.GetMedicinesUseCase
import com.rudresh.xpulse.core.domain.usecase.GrantAccessUseCase
import com.rudresh.xpulse.core.domain.usecase.RevokeAccessUseCase
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class Reminder(
    val id: String,
    val medicineName: String,
    val dose: String,
    val atMillis: Long,
    val taken: Boolean = false,
    val skipped: Boolean = false,
)

data class DraftMedicine(
    val id: String,
    val name: String,
    val dose: String,
    val frequency: String,
)

data class PatientState(
    val medicines: List<Medicine> = emptyList(),
    val medicinesLoading: Boolean = false,
    val reminders: List<Reminder> = emptyList(),
    val waterGlasses: Int = 0,
    val waterTarget: Int = 8,
    val grantId: String? = null,
    val grantRevoked: Boolean = false,
    val grantBusy: Boolean = false,
    val audit: List<AuditEntry> = emptyList(),
    val auditLoading: Boolean = false,
    val message: String? = null,
    val scanning: Boolean = false,
    val checkedIn: Boolean = false,
    val capturedImageUri: String? = null,
    val extracting: Boolean = false,
    val draftMedicines: List<DraftMedicine> = emptyList(),
    val addedMessage: String? = null,
    val age: String = "",
    val city: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val medicalConditions: Set<String> = emptySet(),
    val abhaConnected: Boolean = false,
    val insuranceConnected: Boolean = false,
    val fingerprintLockEnabled: Boolean = false,
)

private val REMINDER_OFFSETS_MINUTES = listOf(-190L, -55L, 20L, 95L, 240L, 400L)

private val EXTRACTION_POOL = listOf(
    Triple("Amoxicillin 500mg", "1 capsule", "Three times daily"),
    Triple("Paracetamol 650mg", "1 tablet", "As needed"),
    Triple("Cetirizine 10mg", "1 tablet", "Once daily"),
    Triple("Azithromycin 250mg", "1 tablet", "Once daily"),
    Triple("Pantoprazole 40mg", "1 tablet", "Once daily · before breakfast"),
)

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val getMedicines: GetMedicinesUseCase,
    private val grantAccess: GrantAccessUseCase,
    private val revokeAccess: RevokeAccessUseCase,
    private val getAuditLog: GetAuditLogUseCase,
    private val checkInUseCase: CheckInUseCase,
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
                is Result.Success -> _state.value = _state.value.copy(
                    medicines = r.data,
                    medicinesLoading = false,
                    reminders = buildReminders(r.data),
                )
                is Result.Error -> _state.value = _state.value.copy(message = r.message, medicinesLoading = false)
            }
        }
    }

    private fun buildReminders(medicines: List<Medicine>): List<Reminder> {
        val now = System.currentTimeMillis()
        var offsetIndex = 0
        val reminders = mutableListOf<Reminder>()
        medicines.filterNot { it.isPrn }.forEach { medicine ->
            val slots = if (medicine.frequency.contains("Twice", ignoreCase = true)) 2 else 1
            repeat(slots) {
                val offset = REMINDER_OFFSETS_MINUTES[offsetIndex % REMINDER_OFFSETS_MINUTES.size]
                reminders.add(
                    Reminder(
                        id = "${medicine.id}_$offsetIndex",
                        medicineName = medicine.name,
                        dose = medicine.dose,
                        atMillis = now + offset * 60_000,
                    ),
                )
                offsetIndex++
            }
        }
        return reminders.sortedBy { it.atMillis }
    }

    fun markTaken(reminderId: String) {
        _state.value = _state.value.copy(
            reminders = _state.value.reminders.map {
                if (it.id == reminderId) it.copy(taken = true, skipped = false) else it
            },
        )
    }

    fun markSkipped(reminderId: String) {
        _state.value = _state.value.copy(
            reminders = _state.value.reminders.map {
                if (it.id == reminderId) it.copy(skipped = true, taken = false) else it
            },
        )
    }

    fun addWaterGlass() {
        val next = (_state.value.waterGlasses + 1).coerceAtMost(_state.value.waterTarget + 4)
        _state.value = _state.value.copy(waterGlasses = next)
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

    fun checkIn() {
        if (_state.value.scanning) return
        viewModelScope.launch {
            _state.value = _state.value.copy(scanning = true)
            when (val r = checkInUseCase(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(scanning = false, checkedIn = true)
                is Result.Error -> _state.value = _state.value.copy(scanning = false, message = r.message)
            }
        }
    }

    fun resetCheckIn() {
        _state.value = _state.value.copy(checkedIn = false)
    }

    fun onImagePicked(uriString: String) {
        _state.value = _state.value.copy(
            capturedImageUri = uriString,
            extracting = true,
            draftMedicines = emptyList(),
            addedMessage = null,
        )
        viewModelScope.launch {
            delay(1600)
            val seed = abs(uriString.hashCode())
            val count = 2 + (seed % 2)
            val drafts = (0 until count).map { i ->
                val (name, dose, frequency) = EXTRACTION_POOL[(seed + i) % EXTRACTION_POOL.size]
                DraftMedicine(id = "draft_${seed}_$i", name = name, dose = dose, frequency = frequency)
            }
            _state.value = _state.value.copy(extracting = false, draftMedicines = drafts)
        }
    }

    fun updateDraft(id: String, name: String? = null, dose: String? = null, frequency: String? = null) {
        _state.value = _state.value.copy(
            draftMedicines = _state.value.draftMedicines.map {
                if (it.id == id) {
                    it.copy(name = name ?: it.name, dose = dose ?: it.dose, frequency = frequency ?: it.frequency)
                } else it
            },
        )
    }

    fun removeDraft(id: String) {
        _state.value = _state.value.copy(draftMedicines = _state.value.draftMedicines.filterNot { it.id == id })
    }

    fun addBlankDraft() {
        _state.value = _state.value.copy(
            draftMedicines = _state.value.draftMedicines + DraftMedicine(id = "draft_${System.currentTimeMillis()}", name = "", dose = "", frequency = ""),
        )
    }

    fun confirmDraft() {
        val newMedicines = _state.value.draftMedicines
            .filter { it.name.isNotBlank() }
            .map { Medicine(id = "m_${System.currentTimeMillis()}_${it.id}", name = it.name, dose = it.dose, frequency = it.frequency, isPrn = false) }
        val updatedMedicines = _state.value.medicines + newMedicines
        _state.value = _state.value.copy(
            medicines = updatedMedicines,
            reminders = buildReminders(updatedMedicines),
            capturedImageUri = null,
            extracting = false,
            draftMedicines = emptyList(),
            addedMessage = "${newMedicines.size} medicine(s) added from your prescription",
        )
    }

    fun cancelCapture() {
        _state.value = _state.value.copy(capturedImageUri = null, extracting = false, draftMedicines = emptyList())
    }

    fun completeOnboarding(age: String, city: String, heightCm: String, weightKg: String, conditions: Set<String>) {
        _state.value = _state.value.copy(
            age = age,
            city = city,
            heightCm = heightCm,
            weightKg = weightKg,
            medicalConditions = conditions,
        )
        sessionManager.completeOnboarding()
    }

    fun updateMedicalConditions(conditions: Set<String>) {
        _state.value = _state.value.copy(medicalConditions = conditions)
    }

    fun toggleAbha() {
        _state.value = _state.value.copy(abhaConnected = !_state.value.abhaConnected)
    }

    fun toggleInsurance() {
        _state.value = _state.value.copy(insuranceConnected = !_state.value.insuranceConnected)
    }

    fun toggleFingerprintLock() {
        _state.value = _state.value.copy(fingerprintLockEnabled = !_state.value.fingerprintLockEnabled)
    }
}
