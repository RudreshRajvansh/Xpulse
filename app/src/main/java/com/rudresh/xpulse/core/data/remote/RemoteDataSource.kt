package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.model.User

interface RemoteDataSource {
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun logout()
    suspend fun getMedicines(patientId: String): List<Medicine>
    suspend fun issuePrescription(patientId: String, doctorId: String, items: List<Medicine>): Prescription
    suspend fun getPendingPrescriptions(): List<Prescription>
    suspend fun fulfillPrescription(prescriptionId: String): Prescription
    suspend fun grantAccess(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): AccessGrant
    suspend fun revoke(grantId: String): AccessGrant
    suspend fun verifyGrant(grantId: String): ScopedData
    suspend fun readAuditLog(patientId: String): List<AuditEntry>
    suspend fun getActiveGrants(granteeId: String): List<AccessGrant>
    suspend fun checkIn(patientId: String): Appointment
    suspend fun getQueue(): List<Appointment>
    suspend fun admit(appointmentId: String): Appointment
}
