package com.mosait.ems.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mosait.ems.core.database.entity.InitialAssessmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InitialAssessmentDao {

    @Query("SELECT * FROM initial_assessments WHERE patientId = :patientId")
    fun getByPatientId(patientId: Long): Flow<InitialAssessmentEntity?>

    @Query("SELECT * FROM initial_assessments WHERE patientId = :patientId")
    suspend fun getByPatientIdOnce(patientId: Long): InitialAssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: InitialAssessmentEntity): Long

    @Update
    suspend fun update(assessment: InitialAssessmentEntity)

    @Query("DELETE FROM initial_assessments WHERE patientId = :patientId")
    suspend fun deleteByPatientId(patientId: Long)
}
