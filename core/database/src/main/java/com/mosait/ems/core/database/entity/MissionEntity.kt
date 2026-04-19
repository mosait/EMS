package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val status: String = "IN_PROGRESS",

    val einsatzDatum: LocalDate? = null,
    val einsatzNummer: String = "",
    val einsatzArt: String = "NOTFALLEINSATZ",
    val einsatzArtSonstiges: String = "",

    val rettungsMittel: String = "RTW",
    val rettungsMittelSonstiges: String = "",
    val fahrzeugKennung: String = "",
    val funkKennung: String = "",

    val einsatzOrtStrasse: String = "",
    val einsatzOrtPlz: String = "",
    val einsatzOrtOrt: String = "",
    val einsatzOrtZusatz: String = "",

    val transportZiel: String = "",

    // Personal als JSON-String (einfacher als separate Tabelle für max 3-4 Einträge)
    val personalJson: String = "[]",

    val kmAnfang: Int? = null,
    val kmEnde: Int? = null,

    val zeitAlarm: LocalDateTime? = null,
    val zeitAbfahrt: LocalDateTime? = null,
    val zeitAnkunftEinsatzort: LocalDateTime? = null,
    val zeitAbfahrtEinsatzort: LocalDateTime? = null,
    val zeitAnkunftKrankenhaus: LocalDateTime? = null,
    val zeitFreimeldung: LocalDateTime? = null,
    val zeitEnde: LocalDateTime? = null,

    val sondersignalZumEinsatz: Boolean = false,
    val sondersignalPatientenfahrt: Boolean = false,

    val bemerkungen: String = ""
)
