package com.rudresh.xpulse.core.data.di

import com.rudresh.xpulse.core.data.remote.HttpRemoteDataSource
import com.rudresh.xpulse.core.data.remote.RemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(impl: HttpRemoteDataSource): RemoteDataSource
}
