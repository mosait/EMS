package com.mosait.ems.core.data.repository

import com.mosait.ems.core.model.Patient
import kotlinx.coroutines.flow.Flow

interface PatientRepository {
    fun getPatientsByMission(missionId: Long): Flow<List<Patient>>
    fun getPatientById(id: Long): Flow<Patient?>
    suspend fun getPatientByIdOnce(id: Long): Patient?
    suspend fun createPatient(patient: Patient): Long
    suspend fun updatePatient(patient: Patient)
    suspend fun deletePatient(id: Long)
    suspend fun getPatientCountForMission(missionId: Long): Int
}
