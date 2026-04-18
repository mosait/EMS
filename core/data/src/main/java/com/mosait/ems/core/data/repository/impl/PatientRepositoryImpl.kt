package com.mosait.ems.core.data.repository.impl

import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.database.dao.PatientDao
import com.mosait.ems.core.database.mapper.toDomain
import com.mosait.ems.core.database.mapper.toEntity
import com.mosait.ems.core.model.Patient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val patientDao: PatientDao
) : PatientRepository {

    override fun getPatientsByMission(missionId: Long): Flow<List<Patient>> {
        return patientDao.getPatientsByMission(missionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPatientById(id: Long): Flow<Patient?> {
        return patientDao.getPatientById(id).map { it?.toDomain() }
    }

    override suspend fun getPatientByIdOnce(id: Long): Patient? {
        return patientDao.getPatientByIdOnce(id)?.toDomain()
    }

    override suspend fun createPatient(patient: Patient): Long {
        return patientDao.insertPatient(patient.toEntity())
    }

    override suspend fun updatePatient(patient: Patient) {
        patientDao.updatePatient(patient.toEntity())
    }

    override suspend fun deletePatient(id: Long) {
        patientDao.deletePatientById(id)
    }

    override suspend fun getPatientCountForMission(missionId: Long): Int {
        return patientDao.getPatientCountForMission(missionId)
    }
}
