package com.mosait.ems.core.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Mission(
    val id: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val status: MissionStatus = MissionStatus.IN_PROGRESS,

    // Einsatzdaten
    val einsatzDatum: LocalDate? = null,
    val einsatzNummer: String = "",
    val einsatzArt: EinsatzArt = EinsatzArt.NOTFALLEINSATZ,

    // Rettungsmittel
    val rettungsMittel: RettungsMittel = RettungsMittel.RTW,
    val fahrzeugKennung: String = "",
    val funkKennung: String = "",

    // Einsatzort
    val einsatzOrtStrasse: String = "",
    val einsatzOrtPlz: String = "",
    val einsatzOrtOrt: String = "",
    val einsatzOrtZusatz: String = "",

    // Transportziel
    val transportZiel: String = "",

    // Personal
    val personal: List<PersonalEntry> = emptyList(),

    // Kilometerstand
    val kmAnfang: Int? = null,
    val kmEnde: Int? = null,

    // Zeiten
    val zeitAlarm: LocalDateTime? = null,
    val zeitAbfahrt: LocalDateTime? = null,
    val zeitAnkunftEinsatzort: LocalDateTime? = null,
    val zeitAbfahrtEinsatzort: LocalDateTime? = null,
    val zeitAnkunftKrankenhaus: LocalDateTime? = null,
    val zeitFreimeldung: LocalDateTime? = null,
    val zeitEnde: LocalDateTime? = null,

    // Sondersignal
    val sondersignalZumEinsatz: Boolean = false,
    val sondersignalPatientenfahrt: Boolean = false,

    // Bemerkungen
    val bemerkungen: String = ""
)
