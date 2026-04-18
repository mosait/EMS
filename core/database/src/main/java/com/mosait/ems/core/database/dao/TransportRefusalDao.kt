package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.TransportRefusalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportRefusalDao {

    @Query("SELECT * FROM transport_refusals WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<TransportRefusalEntity?>

    @Query("SELECT * FROM transport_refusals WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): TransportRefusalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refusal: TransportRefusalEntity): Long

    @Update
    suspend fun update(refusal: TransportRefusalEntity)

    @Query("DELETE FROM transport_refusals WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
