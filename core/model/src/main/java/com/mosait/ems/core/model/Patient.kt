package com.mosait.ems.core.model

import java.time.LocalDate

data class Patient(
    val id: Long = 0,
    val missionId: Long = 0,
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    // Personalien
    val nachname: String = "",
    val vorname: String = "",
    val geburtsdatum: LocalDate? = null,
    val geschlecht: Geschlecht = Geschlecht.UNBEKANNT,

    // Versicherungsdaten
    val krankenkasse: String = "",
    val versichertenNummer: String = "",
    val versichertenStatus: String = "",
    val kostentraegerKennung: String = "",
    val betriebsstaetteNummer: String = "",
    val arztNummer: String = "",

    // Adresse
    val strasse: String = "",
    val plz: String = "",
    val ort: String = "",
    val telefon: String = ""
)

enum class Geschlecht {
    MAENNLICH,
    WEIBLICH,
    DIVERS,
    UNBEKANNT
}
