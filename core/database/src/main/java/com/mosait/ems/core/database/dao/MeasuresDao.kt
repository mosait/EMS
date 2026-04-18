package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.MeasuresEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasuresDao {

    @Query("SELECT * FROM measures WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<MeasuresEntity?>

    @Query("SELECT * FROM measures WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): MeasuresEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measures: MeasuresEntity): Long

    @Update
    suspend fun update(measures: MeasuresEntity)

    @Query("DELETE FROM measures WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
