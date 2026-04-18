package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "transport_refusals",
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
data class TransportRefusalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,

    val enabled: Boolean = false,

    val patientName: String = "",
    val geburtsdatum: String = "",
    val geburtsort: String = "",

    val datum: LocalDate? = null,
    val uhrzeit: String = "",

    val lehntBehandlungAb: Boolean = false,
    val lehntTransportAb: Boolean = false,

    val nichtAuszuschliessendeErkrankungen: String = "",
    val moeglicheFolgen: String = "",

    val nameZeugeAngehoeriger: String = "",
    val adresseZeugeAngehoeriger: String = "",
    val nameZeugeRettungsdienst: String = "",
    val nameRettungsdienstNotarzt: String = "",

    val signaturePatient: ByteArray? = null,

    val ort: String = ""
)
