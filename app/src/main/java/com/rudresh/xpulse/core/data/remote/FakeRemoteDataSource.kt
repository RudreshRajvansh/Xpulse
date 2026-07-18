package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.model.User
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRemoteDataSource @Inject constructor() : RemoteDataSource {

    private data class Account(val user: User, val password: String)

    private val accounts = mutableListOf(
        Account(User("u_patient", "Aarav Sharma", "patient@xpulse.in", setOf(Role.PATIENT), null), "password"),
        Account(User("u_doctor", "Dr. Mehta", "doctor@xpulse.in", setOf(Role.DOCTOR), "fac_1"), "password"),
        Account(User("u_reception", "Priya Nair", "reception@xpulse.in", setOf(Role.RECEPTIONIST), "fac_1"), "password"),
        Account(User("u_pharmacy", "MedPlus Pharmacy", "pharmacy@xpulse.in", setOf(Role.PHARMACY), "fac_1"), "password"),
        Account(User("u_diagnostic", "City Diagnostics", "diagnostic@xpulse.in", setOf(Role.DIAGNOSTIC), "fac_1"), "password"),
        Account(User("u_admin", "Area Admin", "admin@xpulse.in", setOf(Role.ADMIN), "area_1"), "password"),
        Account(User("u_super_admin", "Super Admin", "superadmin@xpulse.in", setOf(Role.SUPER_ADMIN), null), "password"),
        Account(User("u_customer_care", "Support Desk", "care@xpulse.in", setOf(Role.CUSTOMER_CARE), null), "password"),
    )

    private val medicines = mutableMapOf(
        "u_patient" to mutableListOf(
            Medicine("m1", "Metformin 500mg", "1 tablet", "Twice daily", false),
            Medicine("m2", "Telmisartan 40mg", "1 tablet", "Once daily", false),
        ),
    )

    private val allergies = mutableMapOf(
        "u_patient" to listOf("Penicillin", "Sulfa drugs"),
    )

    private val grants = mutableListOf<AccessGrant>()
    private val audit = mutableListOf<AuditEntry>()
    private val prescriptions = mutableListOf<Prescription>()
    private val appointments = mutableListOf<Appointment>()

    override suspend fun login(email: String, password: String): User {
        delay(600)
        return accounts.firstOrNull { it.user.email == email && it.password == password }?.user
            ?: throw NoSuchElementException("Invalid email or password")
    }

    override suspend fun register(name: String, email: String, password: String): User {
        delay(700)
        if (accounts.any { it.user.email.equals(email, ignoreCase = true) }) {
            throw IllegalStateException("An account with this email already exists")
        }
        val user = User(id = "u_${newId()}", name = name, email = email, roles = setOf(Role.PATIENT), scopeId = null)
        accounts.add(Account(user, password))
        return user
    }

    override suspend fun logout() {
        delay(200)
    }

    override suspend fun getMedicines(patientId: String): List<Medicine> {
        delay(400)
        return medicines[patientId].orEmpty()
    }

    override suspend fun issuePrescription(patientId: String, doctorId: String, items: List<Medicine>): Prescription {
        delay(400)
        medicines.getOrPut(patientId) { mutableListOf() }.addAll(items)
        audit.add(entry(doctorId, "Issued a prescription", patientId))
        val prescription = Prescription(newId(), patientId, nameOf(patientId), doctorId, items, now(), fulfilled = false)
        prescriptions.add(prescription)
        return prescription
    }

    override suspend fun getPendingPrescriptions(): List<Prescription> {
        delay(300)
        return prescriptions.filter { !it.fulfilled }.sortedByDescending { it.issuedAt }
    }

    override suspend fun fulfillPrescription(prescriptionId: String): Prescription {
        delay(300)
        val index = prescriptions.indexOfFirst { it.id == prescriptionId }
        if (index == -1) throw NoSuchElementException("Prescription not found")
        val fulfilled = prescriptions[index].copy(fulfilled = true)
        prescriptions[index] = fulfilled
        audit.add(entry("Pharmacy", "Fulfilled a prescription", fulfilled.patientId))
        return fulfilled
    }

    override suspend fun grantAccess(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): AccessGrant {
        delay(300)
        val grant = AccessGrant(newId(), patientId, granteeId, scope, now(), expiresAt, false)
        grants.add(grant)
        audit.add(entry("You", "Granted access", patientId))
        return grant
    }

    override suspend fun revoke(grantId: String): AccessGrant {
        delay(300)
        val index = grants.indexOfFirst { it.id == grantId }
        if (index == -1) throw NoSuchElementException("Grant not found")
        val revoked = grants[index].copy(revoked = true)
        grants[index] = revoked
        audit.add(entry("You", "Revoked access", revoked.patientId))
        return revoked
    }

    override suspend fun verifyGrant(grantId: String): ScopedData {
        delay(300)
        val grant = grants.firstOrNull { it.id == grantId } ?: throw NoSuchElementException("Grant not found")
        if (grant.revoked || now() > grant.expiresAt) {
            audit.add(entry(grant.granteeId, "Access denied", grant.patientId))
            throw IllegalStateException("Access denied")
        }
        audit.add(entry(grant.granteeId, "Viewed medicines and allergies", grant.patientId))
        return ScopedData(nameOf(grant.patientId), medicines[grant.patientId].orEmpty(), allergies[grant.patientId].orEmpty())
    }

    override suspend fun readAuditLog(patientId: String): List<AuditEntry> {
        delay(200)
        return audit.filter { it.subjectId == patientId }.sortedByDescending { it.timestamp }
    }

    override suspend fun getActiveGrants(granteeId: String): List<AccessGrant> {
        delay(200)
        val activeNow = now()
        return grants.filter { it.granteeId == granteeId && !it.revoked && it.expiresAt > activeNow }.sortedByDescending { it.issuedAt }
    }

    override suspend fun checkIn(patientId: String): Appointment {
        delay(1200)
        val appointment = Appointment(newId(), patientId, nameOf(patientId), now())
        appointments.add(appointment)
        audit.add(entry("Reception", "Checked in via QR", patientId))
        return appointment
    }

    override suspend fun getQueue(): List<Appointment> {
        delay(200)
        return appointments.sortedBy { it.checkedInAt }
    }

    override suspend fun admit(appointmentId: String): Appointment {
        delay(300)
        val index = appointments.indexOfFirst { it.id == appointmentId }
        if (index == -1) throw NoSuchElementException("Appointment not found")
        val appointment = appointments.removeAt(index)
        audit.add(entry("Reception", "Admitted for consultation", appointment.patientId))
        return appointment
    }

    private fun nameOf(userId: String) = accounts.firstOrNull { it.user.id == userId }?.user?.name ?: "Patient"

    private fun now() = System.currentTimeMillis()

    private fun newId() = UUID.randomUUID().toString()

    private fun entry(actor: String, action: String, subjectId: String) =
        AuditEntry(newId(), actor, action, subjectId, now())
}
