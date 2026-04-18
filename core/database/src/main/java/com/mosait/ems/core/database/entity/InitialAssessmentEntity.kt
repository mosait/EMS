package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "initial_assessments",
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
data class InitialAssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,

    val notfallgeschehen: String = "",

    val bewusstseinslage: String = "ORIENTIERT",
    val bewusstseinslageText: String = "",

    val kreislaufSchock: Boolean = false,
    val kreislaufStillstand: Boolean = false,
    val kreislaufReanimation: Boolean = false,
    val kreislaufSonstigesText: String = "",

    val rrSystolisch: Int? = null,
    val rrDiastolisch: Int? = null,
    val puls: Int? = null,
    val spO2: Int? = null,
    val atemfrequenz: Int? = null,
    val blutzucker: Int? = null,
    val temperatur: Float? = null,
    val messwertZeit: String? = null,

    val gcsAugen: Int = 4,
    val gcsVerbal: Int = 5,
    val gcsMotorik: Int = 6,

    val pupilleLinks: String = "MITTEL",
    val pupilleRechts: String = "MITTEL",
    val pupillenLichtreaktionLinks: Boolean = true,
    val pupillenLichtreaktionRechts: Boolean = true,

    val ekg: String = "SINUS",
    val ekgSonstigesText: String = "",

    val schmerzSkala: Int = 0,

    val atmung: String = "SPONTAN",
    val atmungSonstigesText: String = ""
)
