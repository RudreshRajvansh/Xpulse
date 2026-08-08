package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.model.Recommendation
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.model.User

interface RemoteDataSource {
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, phone: String, password: String): User
    suspend fun requestPasswordReset(email: String): String
    suspend fun resetPassword(email: String, otp: String, newPassword: String)
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
    suspend fun getReceptionToken(): String
    suspend fun checkIn(patientId: String, token: String): Appointment
    suspend fun getQueue(): List<Appointment>
    suspend fun admit(appointmentId: String): Appointment
    suspend fun orderLabTest(patientId: String, testName: String, orderedBy: String): LabOrder
    suspend fun getLabOrders(): List<LabOrder>
    suspend fun getLabOrdersForPatient(patientId: String): List<LabOrder>
    suspend fun collectSample(orderId: String): LabOrder
    suspend fun completeLabOrder(orderId: String, resultSummary: String): LabOrder
    suspend fun getPlatformStats(): PlatformStats
    suspend fun getUsers(): List<User>
    suspend fun createStaffAccount(name: String, email: String, password: String, role: Role, scopeId: String?): User
    suspend fun raiseTicket(userId: String, subject: String, detail: String): SupportTicket
    suspend fun getTickets(): List<SupportTicket>
    suspend fun getTicketsForUser(userId: String): List<SupportTicket>
    suspend fun resolveTicket(ticketId: String, resolution: String): SupportTicket
    suspend fun getRecommendations(patientId: String): List<Recommendation>
    suspend fun getProfile(patientId: String): PatientProfile
    suspend fun saveProfile(patientId: String, profile: PatientProfile): PatientProfile
    suspend fun getReports(patientId: String): List<MedicalReport>
    suspend fun addReport(patientId: String, category: String, label: String, uri: String): MedicalReport
    suspend fun removeReport(reportId: String)
}
