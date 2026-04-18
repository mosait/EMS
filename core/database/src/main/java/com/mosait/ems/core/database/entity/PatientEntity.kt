package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "patients",
    foreignKeys = [
        ForeignKey(
            entity = MissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["missionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("missionId")]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val missionId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val nachname: String = "",
    val vorname: String = "",
    val geburtsdatum: LocalDate? = null,
    val geschlecht: String = "UNBEKANNT",

    val krankenkasse: String = "",
    val versichertenNummer: String = "",
    val versichertenStatus: String = "",
    val kostentraegerKennung: String = "",
    val betriebsstaetteNummer: String = "",
    val arztNummer: String = "",

    val strasse: String = "",
    val plz: String = "",
    val ort: String = "",
    val telefon: String = ""
)
