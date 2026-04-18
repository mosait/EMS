package com.mosait.ems.core.data.repository

import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.model.MissionStatus
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun getAllMissions(): Flow<List<Mission>>
    fun getMissionsByStatus(status: MissionStatus): Flow<List<Mission>>
    fun getMissionById(id: Long): Flow<Mission?>
    suspend fun getMissionByIdOnce(id: Long): Mission?
    suspend fun createMission(mission: Mission): Long
    suspend fun updateMission(mission: Mission)
    suspend fun deleteMission(id: Long)
    suspend fun getLatestMission(): Mission?
}
