package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measures",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId", unique = true)]
)
data class MeasuresEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val selectedMeasuresJson: String = "[]",
    val sauerstoffLiterProMin: Float? = null,
    val medikamente: String = "",
    val sonstige: String = "",
    val sonstigesTexteJson: String = "{}",

    // Ersthelfer
    val ersthelferSuffizient: Boolean = false,
    val ersthelferInsuffizient: Boolean = false,
    val ersthelferAed: Boolean = false,
    val ersthelferKeine: Boolean = false
)
