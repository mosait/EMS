package com.mosait.ems.core.data.di

import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.data.repository.impl.MissionRepositoryImpl
import com.mosait.ems.core.data.repository.impl.PatientRepositoryImpl
import com.mosait.ems.core.data.repository.impl.ProtocolRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindMissionRepository(impl: MissionRepositoryImpl): MissionRepository

    @Binds
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository

    @Binds
    abstract fun bindProtocolRepository(impl: ProtocolRepositoryImpl): ProtocolRepository
}
