package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.InfectionProtocolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfectionProtocolDao {

    @Query("SELECT * FROM infection_protocols WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<InfectionProtocolEntity?>

    @Query("SELECT * FROM infection_protocols WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): InfectionProtocolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(protocol: InfectionProtocolEntity): Long

    @Update
    suspend fun update(protocol: InfectionProtocolEntity)

    @Query("DELETE FROM infection_protocols WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
