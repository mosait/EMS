package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "vital_signs",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class VitalSignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val timestamp: LocalDateTime = LocalDateTime.now(),

    val puls: Int? = null,
    val rrSystolisch: Int? = null,
    val rrDiastolisch: Int? = null,
    val spO2: Int? = null,
    val atemfrequenz: Int? = null,
    val blutzucker: Int? = null,
    val temperatur: Float? = null,
    val ekg: String? = null,
    val gcs: Int? = null,
    val schmerzSkala: Int? = null,
    val bemerkung: String = ""
)
