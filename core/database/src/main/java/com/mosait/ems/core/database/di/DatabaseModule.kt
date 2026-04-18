package com.mosait.ems.core.database.di

import android.content.Context
import androidx.room.Room
import com.mosait.ems.core.database.EmsDatabase
import com.mosait.ems.core.database.dao.DiagnosisDao
import com.mosait.ems.core.database.dao.InitialAssessmentDao
import com.mosait.ems.core.database.dao.InjuryDao
import com.mosait.ems.core.database.dao.MeasuresDao
import com.mosait.ems.core.database.dao.InfectionProtocolDao
import com.mosait.ems.core.database.dao.MissionDao
import com.mosait.ems.core.database.dao.MissionResultDao
import com.mosait.ems.core.database.dao.PatientDao
import com.mosait.ems.core.database.dao.TransportRefusalDao
import com.mosait.ems.core.database.dao.VitalSignDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EmsDatabase {
        return Room.databaseBuilder(
            context,
            EmsDatabase::class.java,
            "ems_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideMissionDao(database: EmsDatabase): MissionDao = database.missionDao()

    @Provides
    fun providePatientDao(database: EmsDatabase): PatientDao = database.patientDao()

    @Provides
    fun provideInitialAssessmentDao(database: EmsDatabase): InitialAssessmentDao = database.initialAssessmentDao()

    @Provides
    fun provideDiagnosisDao(database: EmsDatabase): DiagnosisDao = database.diagnosisDao()

    @Provides
    fun provideInjuryDao(database: EmsDatabase): InjuryDao = database.injuryDao()

    @Provides
    fun provideVitalSignDao(database: EmsDatabase): VitalSignDao = database.vitalSignDao()

    @Provides
    fun provideMeasuresDao(database: EmsDatabase): MeasuresDao = database.measuresDao()

    @Provides
    fun provideMissionResultDao(database: EmsDatabase): MissionResultDao = database.missionResultDao()

    @Provides
    fun provideInfectionProtocolDao(database: EmsDatabase): InfectionProtocolDao = database.infectionProtocolDao()

    @Provides
    fun provideTransportRefusalDao(database: EmsDatabase): TransportRefusalDao = database.transportRefusalDao()
}
