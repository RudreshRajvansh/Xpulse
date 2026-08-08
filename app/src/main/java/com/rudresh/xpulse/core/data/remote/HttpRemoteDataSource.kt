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
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpRemoteDataSource @Inject constructor(
    private val http: HttpClient,
    private val fileReader: FileReader,
) : RemoteDataSource {

    override suspend fun login(email: String, password: String): User {
        val body = JSONObject().put("email", email).put("password", password)
        val response = http.postObject("/auth/login", body)
        http.setToken(response.optString("token"))
        return response.getJSONObject("user").toUser()
    }

    override suspend fun register(name: String, email: String, phone: String, password: String): User {
        val body = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("phone", phone)
            .put("password", password)
        val response = http.postObject("/auth/register", body)
        http.setToken(response.optString("token"))
        return response.getJSONObject("user").toUser()
    }

    override suspend fun requestPasswordReset(email: String): String =
        http.postObject("/auth/password/request", JSONObject().put("email", email)).getString("code")

    override suspend fun resetPassword(email: String, otp: String, newPassword: String) {
        val body = JSONObject()
            .put("email", email)
            .put("otp", otp)
            .put("newPassword", newPassword)
        http.postObject("/auth/password/reset", body)
    }

    override suspend fun logout() {
        runCatching { http.postObject("/auth/logout") }
        http.clearToken()
    }

    override suspend fun getMedicines(patientId: String): List<Medicine> =
        http.getArray("/patients/$patientId/medicines").map { it.toMedicine() }

    override suspend fun issuePrescription(patientId: String, doctorId: String, items: List<Medicine>): Prescription {
        val body = JSONObject()
            .put("patientId", patientId)
            .put("doctorId", doctorId)
            .put("items", JSONArray(items.map { it.toJson() }))
        return http.postObject("/prescriptions", body).toPrescription()
    }

    override suspend fun getPendingPrescriptions(): List<Prescription> =
        http.getArray("/prescriptions/pending").map { it.toPrescription() }

    override suspend fun fulfillPrescription(prescriptionId: String): Prescription =
        http.postObject("/prescriptions/$prescriptionId/fulfill").toPrescription()

    override suspend fun grantAccess(patientId: String, granteeId: String, scope: Set<String>, expiresAt: Long): AccessGrant {
        val body = JSONObject()
            .put("patientId", patientId)
            .put("granteeId", granteeId)
            .put("scope", JSONArray(scope.toList()))
            .put("expiresAt", expiresAt)
        return http.postObject("/grants", body).toAccessGrant()
    }

    override suspend fun revoke(grantId: String): AccessGrant =
        http.postObject("/grants/$grantId/revoke").toAccessGrant()

    override suspend fun verifyGrant(grantId: String): ScopedData =
        http.getObject("/grants/$grantId/verify").toScopedData()

    override suspend fun readAuditLog(patientId: String): List<AuditEntry> =
        http.getArray("/patients/$patientId/audit").map { it.toAuditEntry() }

    override suspend fun getActiveGrants(granteeId: String): List<AccessGrant> =
        http.getArray("/grants?granteeId=$granteeId").map { it.toAccessGrant() }

    override suspend fun getReceptionToken(): String =
        http.getObject("/reception/token").getString("token")

    override suspend fun checkIn(patientId: String, token: String): Appointment {
        val body = JSONObject().put("patientId", patientId).put("token", token)
        return http.postObject("/appointments/checkin", body).toAppointment()
    }

    override suspend fun getQueue(): List<Appointment> =
        http.getArray("/appointments/queue").map { it.toAppointment() }

    override suspend fun admit(appointmentId: String): Appointment =
        http.postObject("/appointments/$appointmentId/admit").toAppointment()

    override suspend fun orderLabTest(patientId: String, testName: String, orderedBy: String): LabOrder {
        val body = JSONObject()
            .put("patientId", patientId)
            .put("testName", testName)
            .put("orderedBy", orderedBy)
        return http.postObject("/lab-orders", body).toLabOrder()
    }

    override suspend fun getLabOrders(): List<LabOrder> =
        http.getArray("/lab-orders").map { it.toLabOrder() }

    override suspend fun getLabOrdersForPatient(patientId: String): List<LabOrder> =
        http.getArray("/patients/$patientId/lab-orders").map { it.toLabOrder() }

    override suspend fun collectSample(orderId: String): LabOrder =
        http.postObject("/lab-orders/$orderId/collect").toLabOrder()

    override suspend fun completeLabOrder(orderId: String, resultSummary: String): LabOrder =
        http.postObject("/lab-orders/$orderId/complete", JSONObject().put("resultSummary", resultSummary)).toLabOrder()

    override suspend fun getPlatformStats(): PlatformStats =
        http.getObject("/admin/stats").toPlatformStats()

    override suspend fun getUsers(): List<User> =
        http.getArray("/admin/users").map { it.toUser() }

    override suspend fun createStaffAccount(name: String, email: String, password: String, role: Role, scopeId: String?): User {
        val body = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("password", password)
            .put("role", role.name)
            .put("scopeId", scopeId ?: JSONObject.NULL)
        return http.postObject("/admin/staff", body).toUser()
    }

    override suspend fun raiseTicket(userId: String, subject: String, detail: String): SupportTicket {
        val body = JSONObject()
            .put("userId", userId)
            .put("subject", subject)
            .put("detail", detail)
        return http.postObject("/tickets", body).toSupportTicket()
    }

    override suspend fun getTickets(): List<SupportTicket> =
        http.getArray("/tickets").map { it.toSupportTicket() }

    override suspend fun getTicketsForUser(userId: String): List<SupportTicket> =
        http.getArray("/patients/$userId/tickets").map { it.toSupportTicket() }

    override suspend fun resolveTicket(ticketId: String, resolution: String): SupportTicket =
        http.postObject("/tickets/$ticketId/resolve", JSONObject().put("resolution", resolution)).toSupportTicket()

    override suspend fun getRecommendations(patientId: String): List<Recommendation> =
        http.getArray("/patients/$patientId/recommendations").map { it.toRecommendation() }

    override suspend fun getProfile(patientId: String): PatientProfile =
        http.getObject("/patients/$patientId/profile").toPatientProfile()

    override suspend fun saveProfile(patientId: String, profile: PatientProfile): PatientProfile =
        http.putObject("/patients/$patientId/profile", profile.toJson()).toPatientProfile()

    override suspend fun getReports(patientId: String): List<MedicalReport> =
        http.getArray("/patients/$patientId/reports").map { it.toMedicalReport() }

    override suspend fun addReport(patientId: String, category: String, label: String, uri: String): MedicalReport {
        val body = JSONObject()
            .put("category", category)
            .put("label", label)
            .put("uri", uri)
            .put("data", fileReader.readAsBase64(uri))
        return http.postObject("/patients/$patientId/reports", body).toMedicalReport()
    }

    override suspend fun removeReport(reportId: String) {
        http.deleteObject("/reports/$reportId")
    }
}
