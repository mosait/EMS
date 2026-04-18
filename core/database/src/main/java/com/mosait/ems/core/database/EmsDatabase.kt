package com.mosait.ems.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mosait.ems.core.database.converter.Converters
import com.mosait.ems.core.database.dao.DiagnosisDao
import com.mosait.ems.core.database.dao.InitialAssessmentDao
import com.mosait.ems.core.database.dao.InjuryDao
import com.mosait.ems.core.database.dao.MeasuresDao
import com.mosait.ems.core.database.dao.MissionDao
import com.mosait.ems.core.database.dao.InfectionProtocolDao
import com.mosait.ems.core.database.dao.MissionResultDao
import com.mosait.ems.core.database.dao.PatientDao
import com.mosait.ems.core.database.dao.TransportRefusalDao
import com.mosait.ems.core.database.dao.VitalSignDao
import com.mosait.ems.core.database.entity.DiagnosisEntity
import com.mosait.ems.core.database.entity.InfectionProtocolEntity
import com.mosait.ems.core.database.entity.InitialAssessmentEntity
import com.mosait.ems.core.database.entity.InjuryEntity
import com.mosait.ems.core.database.entity.MeasuresEntity
import com.mosait.ems.core.database.entity.MissionEntity
import com.mosait.ems.core.database.entity.MissionResultEntity
import com.mosait.ems.core.database.entity.PatientEntity
import com.mosait.ems.core.database.entity.TransportRefusalEntity
import com.mosait.ems.core.database.entity.VitalSignEntity

@Database(
    entities = [
        MissionEntity::class,
        PatientEntity::class,
        InitialAssessmentEntity::class,
        DiagnosisEntity::class,
        InjuryEntity::class,
        VitalSignEntity::class,
        MeasuresEntity::class,
        MissionResultEntity::class,
        InfectionProtocolEntity::class,
        TransportRefusalEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class EmsDatabase : RoomDatabase() {
    abstract fun missionDao(): MissionDao
    abstract fun patientDao(): PatientDao
    abstract fun initialAssessmentDao(): InitialAssessmentDao
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun injuryDao(): InjuryDao
    abstract fun vitalSignDao(): VitalSignDao
    abstract fun measuresDao(): MeasuresDao
    abstract fun missionResultDao(): MissionResultDao
    abstract fun infectionProtocolDao(): InfectionProtocolDao
    abstract fun transportRefusalDao(): TransportRefusalDao
}
