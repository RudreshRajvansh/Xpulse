package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.model.Recommendation

interface ProfileRepository {
    suspend fun getRecommendations(patientId: String): Result<List<Recommendation>>
    suspend fun getProfile(patientId: String): Result<PatientProfile>
    suspend fun saveProfile(patientId: String, profile: PatientProfile): Result<PatientProfile>
    suspend fun getReports(patientId: String): Result<List<MedicalReport>>
    suspend fun addReport(patientId: String, category: String, label: String, uri: String): Result<MedicalReport>
    suspend fun removeReport(reportId: String): Result<Unit>
}
