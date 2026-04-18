package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.DiagnosisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDao {

    @Query("SELECT * FROM diagnoses WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<DiagnosisEntity?>

    @Query("SELECT * FROM diagnoses WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): DiagnosisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnosis: DiagnosisEntity): Long

    @Update
    suspend fun update(diagnosis: DiagnosisEntity)

    @Query("DELETE FROM diagnoses WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
