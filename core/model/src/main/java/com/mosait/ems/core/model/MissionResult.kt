package com.mosait.ems.core.model

import java.time.LocalDateTime

data class MissionResult(
    val id: Long = 0,
    val patientId: Long = 0,

    // Zustand bei Übergabe
    val zustandVerbessert: Boolean = false,
    val zustandUnveraendert: Boolean = false,
    val zustandVerschlechtert: Boolean = false,

    // Transport
    val transportNichtErforderlich: Boolean = false,
    val patientLehntTransportAb: Boolean = false,

    // Notarzt
    val notarztNachgefordert: Boolean = false,
    val notarztAbbestellt: Boolean = false,

    // Sonstiges
    val hausarztInformiert: Boolean = false,
    val todAmNotfallort: Boolean = false,
    val todWaehrendTransport: Boolean = false,
    val sonstigesFreitext: String = "",

    // NACA-Score
    val nacaScore: Int? = null, // 0-7

    // Übergabe
    val uebergabeAn: String = "",
    val uebergabeZeit: LocalDateTime? = null,
    val wertsachen: String = "",

    // Verlaufsbeschreibung
    val verlaufsbeschreibung: String = ""
)
