package com.mosait.ems.core.data.repository

import com.mosait.ems.core.model.Diagnosis
import com.mosait.ems.core.model.InfectionProtocol
import com.mosait.ems.core.model.InitialAssessment
import com.mosait.ems.core.model.Injury
import com.mosait.ems.core.model.Measures
import com.mosait.ems.core.model.MissionResult
import com.mosait.ems.core.model.TransportRefusal
import com.mosait.ems.core.model.VitalSign
import kotlinx.coroutines.flow.Flow

interface ProtocolRepository {
    // Initial Assessment
    fun getInitialAssessment(patientId: Long): Flow<InitialAssessment?>
    suspend fun saveInitialAssessment(assessment: InitialAssessment): Long

    // Diagnosis
    fun getDiagnosis(patientId: Long): Flow<Diagnosis?>
    suspend fun saveDiagnosis(diagnosis: Diagnosis): Long

    // Injury
    fun getInjury(patientId: Long): Flow<Injury?>
    suspend fun saveInjury(injury: Injury): Long

    // Vital Signs
    fun getVitalSigns(patientId: Long): Flow<List<VitalSign>>
    suspend fun addVitalSign(vitalSign: VitalSign): Long
    suspend fun updateVitalSign(vitalSign: VitalSign)
    suspend fun deleteVitalSign(id: Long)

    // Measures
    fun getMeasures(patientId: Long): Flow<Measures?>
    suspend fun saveMeasures(measures: Measures): Long

    // Result
    fun getMissionResult(patientId: Long): Flow<MissionResult?>
    suspend fun saveMissionResult(result: MissionResult): Long

    // Infection Protocol
    fun getInfectionProtocol(patientId: Long): Flow<InfectionProtocol?>
    suspend fun saveInfectionProtocol(protocol: InfectionProtocol): Long

    // Transport Refusal
    fun getTransportRefusal(patientId: Long): Flow<TransportRefusal?>
    suspend fun saveTransportRefusal(refusal: TransportRefusal): Long
}
