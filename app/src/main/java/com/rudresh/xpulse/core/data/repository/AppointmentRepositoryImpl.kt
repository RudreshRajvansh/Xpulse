package com.rudresh.xpulse.core.data.repository

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.repository.AppointmentRepository
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val remote: RemoteDataSource,
) : AppointmentRepository {

    override suspend fun checkIn(patientId: String): Result<Appointment> =
        try {
            Result.Success(remote.checkIn(patientId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not check in", e)
        }

    override suspend fun getQueue(): Result<List<Appointment>> =
        try {
            Result.Success(remote.getQueue())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not load queue", e)
        }

    override suspend fun admit(appointmentId: String): Result<Appointment> =
        try {
            Result.Success(remote.admit(appointmentId))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not admit patient", e)
        }
}
