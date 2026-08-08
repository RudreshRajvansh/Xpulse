package com.rudresh.xpulse.core.domain.usecase

import com.rudresh.xpulse.core.common.Result
import com.rudresh.xpulse.core.domain.model.Appointment
import com.rudresh.xpulse.core.domain.repository.AppointmentRepository
import javax.inject.Inject

class CheckInUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
) {
    suspend operator fun invoke(patientId: String, token: String): Result<Appointment> =
        appointmentRepository.checkIn(patientId, token)
}
