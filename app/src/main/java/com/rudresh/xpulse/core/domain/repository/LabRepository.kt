package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.LabOrder

interface LabRepository {
    suspend fun orderLabTest(patientId: String, testName: String, orderedBy: String): Result<LabOrder>
    suspend fun getLabOrders(): Result<List<LabOrder>>
    suspend fun getLabOrdersForPatient(patientId: String): Result<List<LabOrder>>
    suspend fun collectSample(orderId: String): Result<LabOrder>
    suspend fun completeLabOrder(orderId: String, resultSummary: String): Result<LabOrder>
}
