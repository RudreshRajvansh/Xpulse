package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Medicine
import com.rudresh.xpulse.core.domain.model.Prescription

interface RecordRepository {
    suspend fun getMedicines(patientId: String): Result<List<Medicine>>
    suspend fun issuePrescription(patientId: String, doctorId: String, items: List<Medicine>): Result<Prescription>
}
