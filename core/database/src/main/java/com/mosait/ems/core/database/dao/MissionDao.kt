package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM missions ORDER BY updatedAt DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE status = :status ORDER BY updatedAt DESC")
    fun getMissionsByStatus(status: String): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    fun getMissionById(id: Long): Flow<MissionEntity?>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getMissionByIdOnce(id: Long): MissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteMissionById(id: Long)

    @Query("SELECT COUNT(*) FROM missions")
    suspend fun getMissionCount(): Int

    @Query("SELECT * FROM missions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestMission(): MissionEntity?
}
