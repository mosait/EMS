package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalSignDao {

    @Query("SELECT * FROM vital_signs WHERE patientId = :patientId ORDER BY timestamp ASC")
    fun getByPatientId(patientId: Long): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE id = :id")
    suspend fun getByIdOnce(id: Long): VitalSignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vitalSign: VitalSignEntity): Long

    @Update
    suspend fun update(vitalSign: VitalSignEntity)

    @Delete
    suspend fun delete(vitalSign: VitalSignEntity)

    @Query("DELETE FROM vital_signs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vital_signs WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
