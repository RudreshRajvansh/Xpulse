package com.rudresh.xpulse.core.data.di

import com.rudresh.xpulse.core.data.repository.AccessRepositoryImpl
import com.rudresh.xpulse.core.data.repository.AppointmentRepositoryImpl
import com.rudresh.xpulse.core.data.repository.AuthRepositoryImpl
import com.rudresh.xpulse.core.data.repository.RecordRepositoryImpl
import com.rudresh.xpulse.core.domain.repository.AccessRepository
import com.rudresh.xpulse.core.domain.repository.AppointmentRepository
import com.rudresh.xpulse.core.domain.repository.AuthRepository
import com.rudresh.xpulse.core.domain.repository.RecordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindRecordRepository(impl: RecordRepositoryImpl): RecordRepository

    @Binds
    abstract fun bindAccessRepository(impl: AccessRepositoryImpl): AccessRepository

    @Binds
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository
}
