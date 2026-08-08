package com.rudresh.xpulse.feature.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.usecase.AddReportUseCase
import com.rudresh.xpulse.core.domain.usecase.CheckInUseCase
import com.rudresh.xpulse.core.domain.usecase.GetAuditLogUseCase
import com.rudresh.xpulse.core.domain.usecase.GetMedicinesUseCase
import com.rudresh.xpulse.core.domain.usecase.GetPatientLabOrdersUseCase
import com.rudresh.xpulse.core.domain.usecase.GetProfileUseCase
import com.rudresh.xpulse.core.domain.usecase.GetReportsUseCase
import com.rudresh.xpulse.core.domain.usecase.GetUserTicketsUseCase
import com.rudresh.xpulse.core.domain.usecase.RaiseTicketUseCase
import com.rudresh.xpulse.core.domain.usecase.SaveProfileUseCase
import com.rudresh.xpulse.core.domain.usecase.GrantAccessUseCase
import com.rudresh.xpulse.core.domain.usecase.RevokeAccessUseCase
import com.rudresh.xpulse.core.domain.model.Recommendation
import com.rudresh.xpulse.core.domain.usecase.GetRecommendationsUseCase
import com.rudresh.xpulse.core.ocr.PrescriptionOcr
import com.rudresh.xpulse.core.reminder.ReminderScheduler
import com.rudresh.xpulse.core.security.SecurityPrefs
import com.rudresh.xpulse.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val labOrders: List<LabOrder> = emptyList(),
    val labOrdersLoading: Boolean = false,
    val tickets: List<SupportTicket> = emptyList(),
    val ticketsLoading: Boolean = false,
    val raisingTicket: Boolean = false,
    val ticketMessage: String? = null,
    val abhaConnected: Boolean = false,
    val insuranceConnected: Boolean = false,
    val fingerprintLockEnabled: Boolean = false,
    val reports: List<MedicalReport> = emptyList(),
    val reportsLoading: Boolean = false,
    val uploadingReport: Boolean = false,
    val checkInError: String? = null,
    val remindersScheduled: Boolean = false,
    val extractionNote: String? = null,
    val recommendations: List<Recommendation> = emptyList(),
    val recommendationsLoading: Boolean = false,
)

private val REMINDER_OFFSETS_MINUTES = listOf(-190L, -55L, 20L, 95L, 240L, 400L)

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val getMedicines: GetMedicinesUseCase,
    private val grantAccess: GrantAccessUseCase,
    private val revokeAccess: RevokeAccessUseCase,
    private val getAuditLog: GetAuditLogUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val getPatientLabOrders: GetPatientLabOrdersUseCase,
    private val getUserTickets: GetUserTicketsUseCase,
    private val raiseTicketUseCase: RaiseTicketUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase,
    private val getReportsUseCase: GetReportsUseCase,
    private val addReportUseCase: AddReportUseCase,
    private val reminderScheduler: ReminderScheduler,
    private val prescriptionOcr: PrescriptionOcr,
    private val getRecommendations: GetRecommendationsUseCase,
    private val securityPrefs: SecurityPrefs,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PatientState())
    val state: StateFlow<PatientState> = _state.asStateFlow()

    private val patientId: String get() = sessionManager.currentUser.value?.id.orEmpty()

    init {
        _state.value = _state.value.copy(fingerprintLockEnabled = securityPrefs.lockEnabled.value)
        loadMedicines()
        loadLabOrders()
        loadProfile()
        loadReports()
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val r = getProfileUseCase(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(
                    age = r.data.age,
                    city = r.data.city,
                    heightCm = r.data.heightCm,
                    weightKg = r.data.weightKg,
                    medicalConditions = r.data.conditions,
                    abhaConnected = r.data.abhaConnected,
                    insuranceConnected = r.data.insuranceConnected,
                )
                is Result.Error -> _state.value = _state.value.copy(message = r.message)
            }
        }
    }

    private fun persistProfile(onboarded: Boolean = true) {
        val s = _state.value
        viewModelScope.launch {
            saveProfileUseCase(
                patientId,
                PatientProfile(
                    age = s.age,
                    city = s.city,
                    heightCm = s.heightCm,
                    weightKg = s.weightKg,
                    conditions = s.medicalConditions,
                    abhaConnected = s.abhaConnected,
                    insuranceConnected = s.insuranceConnected,
                    onboarded = onboarded,
                ),
            )
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(recommendationsLoading = true)
            when (val r = getRecommendations(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(recommendations = r.data, recommendationsLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, recommendationsLoading = false)
            }
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _state.value = _state.value.copy(reportsLoading = true)
            when (val r = getReportsUseCase(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(reports = r.data, reportsLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, reportsLoading = false)
            }
        }
    }

    fun addReport(category: String, label: String, uri: String) {
        if (_state.value.uploadingReport) return
        viewModelScope.launch {
            _state.value = _state.value.copy(uploadingReport = true)
            when (val r = addReportUseCase(patientId, category, label, uri)) {
                is Result.Success -> _state.value = _state.value.copy(
                    uploadingReport = false,
                    reports = listOf(r.data) + _state.value.reports,
                    message = "${r.data.category} report added",
                )
                is Result.Error -> _state.value = _state.value.copy(uploadingReport = false, message = r.message)
            }
        }
    }

    fun scheduleReminderNotifications() {
        if (_state.value.remindersScheduled) return
        _state.value.reminders.forEachIndexed { index, reminder ->
            reminderScheduler.schedule(
                requestCode = index + 1,
                medicineName = reminder.medicineName,
                dose = reminder.dose,
                atMillis = reminder.atMillis,
            )
        }
        _state.value = _state.value.copy(remindersScheduled = true)
    }

    fun loadLabOrders() {
        viewModelScope.launch {
            _state.value = _state.value.copy(labOrdersLoading = true)
            when (val r = getPatientLabOrders(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(labOrders = r.data, labOrdersLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, labOrdersLoading = false)
            }
        }
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

    fun checkIn(token: String) {
        if (_state.value.scanning) return
        if (token.isBlank()) {
            _state.value = _state.value.copy(checkInError = "Enter the code shown at reception")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(scanning = true, checkInError = null)
            when (val r = checkInUseCase(patientId, token)) {
                is Result.Success -> _state.value = _state.value.copy(scanning = false, checkedIn = true)
                is Result.Error -> _state.value = _state.value.copy(scanning = false, checkInError = r.message)
            }
        }
    }

    fun resetCheckIn() {
        _state.value = _state.value.copy(checkedIn = false, checkInError = null)
    }

    fun onImagePicked(uriString: String) {
        _state.value = _state.value.copy(
            capturedImageUri = uriString,
            extracting = true,
            draftMedicines = emptyList(),
            addedMessage = null,
            extractionNote = null,
        )
        viewModelScope.launch {
            val outcome = runCatching {
                val lines = prescriptionOcr.readLines(uriString)
                prescriptionOcr.parseMedicines(lines) to lines.size
            }
            outcome.fold(
                onSuccess = { (medicines, lineCount) ->
                    val drafts = medicines.mapIndexed { index, m ->
                        DraftMedicine(
                            id = "draft_${System.currentTimeMillis()}_$index",
                            name = m.name,
                            dose = m.dose,
                            frequency = m.frequency,
                        )
                    }
                    _state.value = _state.value.copy(
                        extracting = false,
                        draftMedicines = drafts.ifEmpty {
                            listOf(DraftMedicine(id = "draft_${System.currentTimeMillis()}", name = "", dose = "", frequency = ""))
                        },
                        extractionNote = when {
                            lineCount == 0 -> "No text found in that image. Try a sharper, well-lit photo."
                            drafts.isEmpty() -> "Read $lineCount lines but found no medicine lines. Add them manually."
                            else -> "Read $lineCount lines, found ${drafts.size} medicine(s). Check each one."
                        },
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        extracting = false,
                        draftMedicines = listOf(DraftMedicine(id = "draft_${System.currentTimeMillis()}", name = "", dose = "", frequency = "")),
                        extractionNote = e.message ?: "Could not read that image",
                    )
                },
            )
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
        persistProfile()
        sessionManager.completeOnboarding()
    }

    fun loadTickets() {
        viewModelScope.launch {
            _state.value = _state.value.copy(ticketsLoading = true)
            when (val r = getUserTickets(patientId)) {
                is Result.Success -> _state.value = _state.value.copy(tickets = r.data, ticketsLoading = false)
                is Result.Error -> _state.value = _state.value.copy(message = r.message, ticketsLoading = false)
            }
        }
    }

    fun raiseTicket(subject: String, detail: String) {
        if (_state.value.raisingTicket || subject.isBlank() || detail.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(raisingTicket = true, ticketMessage = null)
            when (val r = raiseTicketUseCase(patientId, subject, detail)) {
                is Result.Success -> _state.value = _state.value.copy(
                    raisingTicket = false,
                    tickets = listOf(r.data) + _state.value.tickets,
                    ticketMessage = "Ticket raised · our team will get back to you",
                )
                is Result.Error -> _state.value = _state.value.copy(raisingTicket = false, ticketMessage = r.message)
            }
        }
    }

    fun updateMedicalConditions(conditions: Set<String>) {
        _state.value = _state.value.copy(medicalConditions = conditions)
        persistProfile()
    }

    fun toggleAbha() {
        _state.value = _state.value.copy(abhaConnected = !_state.value.abhaConnected)
        persistProfile()
    }

    fun toggleInsurance() {
        _state.value = _state.value.copy(insuranceConnected = !_state.value.insuranceConnected)
        persistProfile()
    }

    fun setFingerprintLock(enabled: Boolean) {
        securityPrefs.setLockEnabled(enabled)
        _state.value = _state.value.copy(fingerprintLockEnabled = enabled)
    }
}
