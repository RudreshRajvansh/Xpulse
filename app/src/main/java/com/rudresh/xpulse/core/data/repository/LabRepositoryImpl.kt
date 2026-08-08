package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.LabOrder
import com.rudresh.xpulse.core.domain.repository.LabRepository
import javax.inject.Inject

class LabRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : LabRepository {

    override suspend fun orderLabTest(patientId: String, testName: String, orderedBy: String): Result<LabOrder> =
        try {
            Result.Success(remote.orderLabTest(patientId, testName, orderedBy))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not order lab test", e)
        }

    override suspend fun getLabOrders(): Result<List<LabOrder>> =
        try {
            Result.Success(remote.getLabOrders())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load lab orders", e)
        }

    override suspend fun getLabOrdersForPatient(patientId: String): Result<List<LabOrder>> =
        try {
            Result.Success(remote.getLabOrdersForPatient(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load lab reports", e)
        }

    override suspend fun collectSample(orderId: String): Result<LabOrder> =
        try {
            Result.Success(remote.collectSample(orderId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not mark sample collected", e)
        }

    override suspend fun completeLabOrder(orderId: String, resultSummary: String): Result<LabOrder> =
        try {
            Result.Success(remote.completeLabOrder(orderId, resultSummary))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not publish report", e)
        }
}
