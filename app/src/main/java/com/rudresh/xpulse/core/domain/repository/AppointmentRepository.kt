package com.rudresh.xpulse.core.domain.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Appointment

interface AppointmentRepository {
    suspend fun getReceptionToken(): Result<String>
    suspend fun checkIn(patientId: String, token: String): Result<Appointment>
    suspend fun getQueue(): Result<List<Appointment>>
    suspend fun admit(appointmentId: String): Result<Appointment>
}
