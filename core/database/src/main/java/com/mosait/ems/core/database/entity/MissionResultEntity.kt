package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "mission_results",
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
data class MissionResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,

    val zustandVerbessert: Boolean = false,
    val zustandUnveraendert: Boolean = false,
    val zustandVerschlechtert: Boolean = false,

    val transportNichtErforderlich: Boolean = false,
    val patientLehntTransportAb: Boolean = false,

    val notarztNachgefordert: Boolean = false,
    val notarztAbbestellt: Boolean = false,

    val hausarztInformiert: Boolean = false,
    val todAmNotfallort: Boolean = false,
    val todWaehrendTransport: Boolean = false,
    val sonstigesFreitext: String = "",

    val nacaScore: Int? = null,

    val uebergabeAn: String = "",
    val uebergabeZeit: LocalDateTime? = null,
    val wertsachen: String = "",

    val verlaufsbeschreibung: String = ""
)
