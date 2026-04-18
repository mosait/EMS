package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.MissionResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionResultDao {

    @Query("SELECT * FROM mission_results WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<MissionResultEntity?>

    @Query("SELECT * FROM mission_results WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): MissionResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: MissionResultEntity): Long

    @Update
    suspend fun update(result: MissionResultEntity)

    @Query("DELETE FROM mission_results WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
