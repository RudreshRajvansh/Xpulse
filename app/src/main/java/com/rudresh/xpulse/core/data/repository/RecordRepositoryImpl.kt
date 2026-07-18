package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.Prescription
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : RecordRepository {

    override suspend fun getMedicines(patientId: String): Result<List<Medicine>> =
        try {
            Result.Success(remote.getMedicines(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load medicines", e)
        }

    override suspend fun issuePrescription(patientId: String, doctorId: String, items: List<Medicine>): Result<Prescription> =
        try {
            Result.Success(remote.issuePrescription(patientId, doctorId, items))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not issue prescription", e)
        }

    override suspend fun getPendingPrescriptions(): Result<List<Prescription>> =
        try {
            Result.Success(remote.getPendingPrescriptions())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load prescriptions", e)
        }

    override suspend fun fulfillPrescription(prescriptionId: String): Result<Prescription> =
        try {
            Result.Success(remote.fulfillPrescription(prescriptionId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not fulfill prescription", e)
        }
}
