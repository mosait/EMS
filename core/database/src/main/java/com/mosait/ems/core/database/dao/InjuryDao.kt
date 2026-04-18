package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.InjuryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InjuryDao {

    @Query("SELECT * FROM injuries WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<InjuryEntity?>

    @Query("SELECT * FROM injuries WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): InjuryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(injury: InjuryEntity): Long

    @Update
    suspend fun update(injury: InjuryEntity)

    @Query("DELETE FROM injuries WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
