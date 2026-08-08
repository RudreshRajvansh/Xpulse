package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.MedicalReport
import com.rudresh.xpulse.core.domain.model.PatientProfile
import com.rudresh.xpulse.core.domain.model.Recommendation
import com.rudresh.xpulse.core.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : ProfileRepository {

    override suspend fun getRecommendations(patientId: String): Result<List<Recommendation>> =
        try {
            Result.Success(remote.getRecommendations(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load recommendations", e)
        }

    override suspend fun getProfile(patientId: String): Result<PatientProfile> =
        try {
            Result.Success(remote.getProfile(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load your profile", e)
        }

    override suspend fun saveProfile(patientId: String, profile: PatientProfile): Result<PatientProfile> =
        try {
            Result.Success(remote.saveProfile(patientId, profile))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not save your profile", e)
        }

    override suspend fun getReports(patientId: String): Result<List<MedicalReport>> =
        try {
            Result.Success(remote.getReports(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load your reports", e)
        }

    override suspend fun addReport(patientId: String, category: String, label: String, uri: String): Result<MedicalReport> =
        try {
            Result.Success(remote.addReport(patientId, category, label, uri))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not upload the report", e)
        }

    override suspend fun removeReport(reportId: String): Result<Unit> =
        try {
            Result.Success(remote.removeReport(reportId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not remove the report", e)
        }
}
