package com.mosait.ems.core.model

import java.time.LocalDateTime

data class InitialAssessment(
    val id: Long = 0,
    val patientId: Long = 0,

    // Notfallgeschehen
    val notfallgeschehen: String = "",

    // Bewusstseinslage
    val bewusstseinslage: Bewusstseinslage = Bewusstseinslage.ORIENTIERT,
    val bewusstseinslageText: String = "",

    // Kreislauf
    val kreislaufSchock: Boolean = false,
    val kreislaufStillstand: Boolean = false,
    val kreislaufReanimation: Boolean = false,
    val kreislaufSonstigesText: String = "",

    // Messwerte
    val rrSystolisch: Int? = null,
    val rrDiastolisch: Int? = null,
    val puls: Int? = null,
    val spO2: Int? = null,
    val atemfrequenz: Int? = null,
    val blutzucker: Int? = null,
    val temperatur: Float? = null,
    val messwertZeit: LocalDateTime? = null,

    // GCS
    val gcsAugen: Int = 4,     // 1-4
    val gcsVerbal: Int = 5,    // 1-5
    val gcsMotorik: Int = 6,   // 1-6

    // Pupillen
    val pupilleLinks: PupillenStatus = PupillenStatus.MITTEL,
    val pupilleRechts: PupillenStatus = PupillenStatus.MITTEL,
    val pupillenLichtreaktionLinks: Boolean = true,
    val pupillenLichtreaktionRechts: Boolean = true,

    // EKG
    val ekg: EkgRhythmus = EkgRhythmus.SINUS,
    val ekgSonstigesText: String = "",

    // Schmerz
    val schmerzSkala: Int = 0, // 0-10 NRS

    // Atmung
    val atmung: AtmungStatus = AtmungStatus.SPONTAN,
    val atmungSonstigesText: String = ""
)

enum class Bewusstseinslage {
    ORIENTIERT,
    GETRUEBT,
    BEWUSSTLOS,
    REIZLOS,
    SONSTIGES
}

enum class PupillenStatus {
    ENG,
    MITTEL,
    WEIT,
    ENTRUNDET
}

enum class EkgRhythmus {
    SINUS,
    ABSOLUTE_ARRHYTHMIE,
    BRADYKARDIE,
    TACHYKARDIE,
    VENTRIKULAERE_TACHYKARDIE,
    KAMMERFLIMMERN,
    ASYSTOLIE,
    PEA,
    SCHRITTMACHER,
    SONSTIGES
}

enum class AtmungStatus {
    SPONTAN,
    ATEMNOT,
    HYPERVENTILATION,
    ATEMSTILLSTAND,
    BEATMET,
    SONSTIGES
}
