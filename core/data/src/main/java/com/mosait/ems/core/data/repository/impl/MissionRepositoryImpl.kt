package com.mosait.ems.core.data.repository.impl

import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.database.dao.MissionDao
import com.mosait.ems.core.database.mapper.toDomain
import com.mosait.ems.core.database.mapper.toEntity
import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.model.MissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionRepositoryImpl @Inject constructor(
    private val missionDao: MissionDao
) : MissionRepository {

    override fun getAllMissions(): Flow<List<Mission>> {
        return missionDao.getAllMissions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMissionsByStatus(status: MissionStatus): Flow<List<Mission>> {
        return missionDao.getMissionsByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMissionById(id: Long): Flow<Mission?> {
        return missionDao.getMissionById(id).map { it?.toDomain() }
    }

    override suspend fun getMissionByIdOnce(id: Long): Mission? {
        return missionDao.getMissionByIdOnce(id)?.toDomain()
    }

    override suspend fun createMission(mission: Mission): Long {
        val now = LocalDateTime.now()
        return missionDao.insertMission(
            mission.copy(createdAt = now, updatedAt = now).toEntity()
        )
    }

    override suspend fun updateMission(mission: Mission) {
        missionDao.updateMission(
            mission.copy(updatedAt = LocalDateTime.now()).toEntity()
        )
    }

    override suspend fun deleteMission(id: Long) {
        missionDao.deleteMissionById(id)
    }

    override suspend fun getLatestMission(): Mission? {
        return missionDao.getLatestMission()?.toDomain()
    }
}
