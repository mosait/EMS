package com.mosait.ems.core.data.repository.impl

import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.database.dao.DiagnosisDao
import com.mosait.ems.core.database.dao.InfectionProtocolDao
import com.mosait.ems.core.database.dao.InitialAssessmentDao
import com.mosait.ems.core.database.dao.InjuryDao
import com.mosait.ems.core.database.dao.MeasuresDao
import com.mosait.ems.core.database.dao.MissionDao
import com.mosait.ems.core.database.dao.MissionResultDao
import com.mosait.ems.core.database.dao.PatientDao
import com.mosait.ems.core.database.dao.TransportRefusalDao
import com.mosait.ems.core.database.dao.VitalSignDao
import com.mosait.ems.core.database.mapper.toDomain
import com.mosait.ems.core.database.mapper.toEntity
import com.mosait.ems.core.model.MissionStatus
import java.time.LocalDateTime
import com.mosait.ems.core.model.Diagnosis
import com.mosait.ems.core.model.InfectionProtocol
import com.mosait.ems.core.model.InitialAssessment
import com.mosait.ems.core.model.Injury
import com.mosait.ems.core.model.Measures
import com.mosait.ems.core.model.MissionResult
import com.mosait.ems.core.model.TransportRefusal
import com.mosait.ems.core.model.VitalSign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtocolRepositoryImpl @Inject constructor(
    private val initialAssessmentDao: InitialAssessmentDao,
    private val diagnosisDao: DiagnosisDao,
    private val injuryDao: InjuryDao,
    private val vitalSignDao: VitalSignDao,
    private val measuresDao: MeasuresDao,
    private val missionResultDao: MissionResultDao,
    private val infectionProtocolDao: InfectionProtocolDao,
    private val transportRefusalDao: TransportRefusalDao,
    private val patientDao: PatientDao,
    private val missionDao: MissionDao
) : ProtocolRepository {

    private suspend fun revertMissionIfExported(patientId: Long) {
        val patient = patientDao.getPatientByIdOnce(patientId) ?: return
        val mission = missionDao.getMissionByIdOnce(patient.missionId) ?: return
        if (mission.status == MissionStatus.EXPORTED.name) {
            missionDao.updateMission(mission.copy(
                status = MissionStatus.IN_PROGRESS.name,
                updatedAt = LocalDateTime.now()
            ))
        }
    }

    // ---- Initial Assessment ----

    override fun getInitialAssessment(patientId: Long): Flow<InitialAssessment?> {
        return initialAssessmentDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveInitialAssessment(assessment: InitialAssessment): Long {
        val existing = initialAssessmentDao.getByPatientIdOnce(assessment.patientId)
        val entity = assessment.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(assessment.patientId)
        return initialAssessmentDao.insert(entity)
    }

    // ---- Diagnosis ----

    override fun getDiagnosis(patientId: Long): Flow<Diagnosis?> {
        return diagnosisDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveDiagnosis(diagnosis: Diagnosis): Long {
        val existing = diagnosisDao.getByPatientIdOnce(diagnosis.patientId)
        val entity = diagnosis.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(diagnosis.patientId)
        return diagnosisDao.insert(entity)
    }

    // ---- Injury ----

    override fun getInjury(patientId: Long): Flow<Injury?> {
        return injuryDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveInjury(injury: Injury): Long {
        val existing = injuryDao.getByPatientIdOnce(injury.patientId)
        val entity = injury.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(injury.patientId)
        return injuryDao.insert(entity)
    }

    // ---- Vital Signs ----

    override fun getVitalSigns(patientId: Long): Flow<List<VitalSign>> {
        return vitalSignDao.getByPatientId(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addVitalSign(vitalSign: VitalSign): Long {
        revertMissionIfExported(vitalSign.patientId)
        return vitalSignDao.insert(vitalSign.toEntity())
    }

    override suspend fun updateVitalSign(vitalSign: VitalSign) {
        revertMissionIfExported(vitalSign.patientId)
        vitalSignDao.update(vitalSign.toEntity())
    }

    override suspend fun deleteVitalSign(id: Long) {
        vitalSignDao.deleteById(id)
    }

    // ---- Measures ----

    override fun getMeasures(patientId: Long): Flow<Measures?> {
        return measuresDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveMeasures(measures: Measures): Long {
        val existing = measuresDao.getByPatientIdOnce(measures.patientId)
        val entity = measures.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(measures.patientId)
        return measuresDao.insert(entity)
    }

    // ---- Result ----

    override fun getMissionResult(patientId: Long): Flow<MissionResult?> {
        return missionResultDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveMissionResult(result: MissionResult): Long {
        val existing = missionResultDao.getByPatientIdOnce(result.patientId)
        val entity = result.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(result.patientId)
        return missionResultDao.insert(entity)
    }

    // ---- Infection Protocol ----

    override fun getInfectionProtocol(patientId: Long): Flow<InfectionProtocol?> {
        return infectionProtocolDao.getByPatientId(patientId).map { it?.toDomain() }
    }

    override suspend fun saveInfectionProtocol(protocol: InfectionProtocol): Long {
        val existing = infectionProtocolDao.getByPatientIdOnce(protocol.patientId)
        val entity = protocol.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(protocol.patientId)
        return infectionProtocolDao.insert(entity)
    }

    // ---- Transport Refusal ----
    override fun getTransportRefusal(patientId: Long): Flow<TransportRefusal?> {
        return transportRefusalDao.getByPatientId(patientId).map { it?.toDomain() }
    }
    override suspend fun saveTransportRefusal(refusal: TransportRefusal): Long {
        val existing = transportRefusalDao.getByPatientIdOnce(refusal.patientId)
        val entity = refusal.toEntity().let {
            if (existing != null) it.copy(id = existing.id) else it
        }
        revertMissionIfExported(refusal.patientId)
        return transportRefusalDao.insert(entity)
    }
}
