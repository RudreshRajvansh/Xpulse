package com.rudresh.xpulse.core.data.remote

import com.rudresh.xpulse.core.domain.model.AccessGrant
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.model.AuditEntry
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.model.LabOrderStatus
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.model.PlatformStats
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.model.Recommendation
import com.rudresh.xpulse.core.domain.model.Role
import com.rudresh.xpulse.core.domain.model.ScopedData
import com.rudresh.xpulse.core.domain.model.SupportTicket
import com.rudresh.xpulse.core.domain.model.TicketStatus
import com.rudresh.xpulse.core.domain.model.User
import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.stringOrNull(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

fun JSONObject.longOrNull(key: String): Long? = if (isNull(key)) null else optLong(key)

fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }

fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { transform(getJSONObject(it)) }

fun JSONObject.toUser(): User = User(
    id = getString("id"),
    name = getString("name"),
    email = getString("email"),
    roles = optJSONArray("roles")?.toStringList()?.mapNotNull { runCatching { Role.valueOf(it) }.getOrNull() }?.toSet().orEmpty(),
    scopeId = stringOrNull("scopeId"),
    phone = stringOrNull("phone"),
)

fun JSONObject.toMedicine(): Medicine = Medicine(
    id = getString("id"),
    name = getString("name"),
    dose = optString("dose"),
    frequency = optString("frequency"),
    isPrn = optBoolean("isPrn"),
)

fun Medicine.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("dose", dose)
    .put("frequency", frequency)
    .put("isPrn", isPrn)

fun JSONObject.toPrescription(): Prescription = Prescription(
    id = getString("id"),
    patientId = getString("patientId"),
    patientName = optString("patientName"),
    doctorId = optString("doctorId"),
    items = optJSONArray("items")?.map { it.toMedicine() }.orEmpty(),
    issuedAt = optLong("issuedAt"),
    fulfilled = optBoolean("fulfilled"),
)

fun JSONObject.toAccessGrant(): AccessGrant = AccessGrant(
    id = getString("id"),
    patientId = getString("patientId"),
    granteeId = optString("granteeId"),
    scope = optJSONArray("scope")?.toStringList()?.toSet().orEmpty(),
    issuedAt = optLong("issuedAt"),
    expiresAt = optLong("expiresAt"),
    revoked = optBoolean("revoked"),
)

fun JSONObject.toAuditEntry(): AuditEntry = AuditEntry(
    id = getString("id"),
    actor = optString("actor"),
    action = optString("action"),
    subjectId = optString("subjectId"),
    timestamp = optLong("timestamp"),
)

fun JSONObject.toAppointment(): Appointment = Appointment(
    id = getString("id"),
    patientId = getString("patientId"),
    patientName = optString("patientName"),
    checkedInAt = optLong("checkedInAt"),
)

fun JSONObject.toLabOrder(): LabOrder = LabOrder(
    id = getString("id"),
    patientId = getString("patientId"),
    patientName = optString("patientName"),
    testName = optString("testName"),
    orderedBy = optString("orderedBy"),
    orderedAt = optLong("orderedAt"),
    status = runCatching { LabOrderStatus.valueOf(optString("status")) }.getOrDefault(LabOrderStatus.ORDERED),
    resultSummary = stringOrNull("resultSummary"),
    completedAt = longOrNull("completedAt"),
)

fun JSONObject.toSupportTicket(): SupportTicket = SupportTicket(
    id = getString("id"),
    raisedBy = optString("raisedBy"),
    raisedByName = optString("raisedByName"),
    subject = optString("subject"),
    detail = optString("detail"),
    status = runCatching { TicketStatus.valueOf(optString("status")) }.getOrDefault(TicketStatus.OPEN),
    createdAt = optLong("createdAt"),
    resolution = stringOrNull("resolution"),
    resolvedAt = longOrNull("resolvedAt"),
)

fun JSONObject.toMedicalReport(): MedicalReport = MedicalReport(
    id = getString("id"),
    patientId = getString("patientId"),
    category = optString("category"),
    label = optString("label"),
    uri = optString("uri"),
    addedAt = optLong("addedAt"),
    fileBytes = optInt("fileBytes"),
    stored = optBoolean("stored"),
)

fun JSONObject.toPatientProfile(): PatientProfile = PatientProfile(
    age = optString("age"),
    city = optString("city"),
    heightCm = optString("heightCm"),
    weightKg = optString("weightKg"),
    conditions = optJSONArray("conditions")?.toStringList()?.toSet().orEmpty(),
    abhaConnected = optBoolean("abhaConnected"),
    insuranceConnected = optBoolean("insuranceConnected"),
    onboarded = optBoolean("onboarded"),
)

fun PatientProfile.toJson(): JSONObject = JSONObject()
    .put("age", age)
    .put("city", city)
    .put("heightCm", heightCm)
    .put("weightKg", weightKg)
    .put("conditions", JSONArray(conditions.toList()))
    .put("abhaConnected", abhaConnected)
    .put("insuranceConnected", insuranceConnected)
    .put("onboarded", onboarded)

fun JSONObject.toScopedData(): ScopedData = ScopedData(
    patientName = optString("patientName"),
    medicines = optJSONArray("medicines")?.map { it.toMedicine() }.orEmpty(),
    allergies = optJSONArray("allergies")?.toStringList().orEmpty(),
    profile = optJSONObject("profile")?.toPatientProfile() ?: PatientProfile(),
    labOrders = optJSONArray("labOrders")?.map { it.toLabOrder() }.orEmpty(),
    reports = optJSONArray("reports")?.map { it.toMedicalReport() }.orEmpty(),
)

fun JSONObject.toRecommendation(): Recommendation = Recommendation(
    title = optString("title"),
    specialty = optString("specialty"),
    reason = optString("reason"),
    urgency = optString("urgency"),
)

fun JSONObject.toPlatformStats(): PlatformStats = PlatformStats(
    totalUsers = optInt("totalUsers"),
    patients = optInt("patients"),
    staff = optInt("staff"),
    prescriptionsIssued = optInt("prescriptionsIssued"),
    prescriptionsPending = optInt("prescriptionsPending"),
    labOrdersOpen = optInt("labOrdersOpen"),
    labOrdersCompleted = optInt("labOrdersCompleted"),
    activeGrants = optInt("activeGrants"),
    revokedGrants = optInt("revokedGrants"),
    waitingInQueue = optInt("waitingInQueue"),
    openTickets = optInt("openTickets"),
)
