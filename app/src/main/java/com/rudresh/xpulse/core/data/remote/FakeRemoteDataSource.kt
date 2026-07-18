package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.domain.model.AccessGrant
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

    private val accounts = listOf(
        Account(User("u_patient", "Aarav Sharma", "patient@xpulse.in", setOf(Role.PATIENT), null), "password"),
        Account(User("u_doctor", "Dr. Mehta", "doctor@xpulse.in", setOf(Role.DOCTOR), "fac_1"), "password"),
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

    override suspend fun login(email: String, password: String): User {
        delay(600)
        return accounts.firstOrNull { it.user.email == email && it.password == password }?.user
            ?: throw NoSuchElementException("Invalid email or password")
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
        return Prescription(newId(), patientId, doctorId, items, now())
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
        return ScopedData(medicines[grant.patientId].orEmpty(), allergies[grant.patientId].orEmpty())
    }

    override suspend fun readAuditLog(patientId: String): List<AuditEntry> {
        delay(200)
        return audit.filter { it.subjectId == patientId }.sortedByDescending { it.timestamp }
    }

    private fun now() = System.currentTimeMillis()

    private fun newId() = UUID.randomUUID().toString()

    private fun entry(actor: String, action: String, subjectId: String) =
        AuditEntry(newId(), actor, action, subjectId, now())
}
