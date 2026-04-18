package com.mosait.ems.core.model

import java.time.LocalDate
import java.time.LocalTime

data class TransportRefusal(
    val id: Long = 0,
    val patientId: Long = 0,

    // Aktiviert
    val enabled: Boolean = false,

    // Patientendaten (auto-filled, editable)
    val patientName: String = "",
    val geburtsdatum: String = "",
    val geburtsort: String = "",

    // Datum/Zeit der Aufklärung
    val datum: LocalDate? = null,
    val uhrzeit: LocalTime? = null,

    // Was wird abgelehnt
    val lehntBehandlungAb: Boolean = false,
    val lehntTransportAb: Boolean = false,

    // Freitextfelder
    val nichtAuszuschliessendeErkrankungen: String = "",
    val moeglicheFolgen: String = "",

    // Unterschriftsbereich
    val nameZeugeAngehoeriger: String = "",
    val adresseZeugeAngehoeriger: String = "",
    val nameZeugeRettungsdienst: String = "",
    val nameRettungsdienstNotarzt: String = "",

    // Unterschrift Patient (PNG bytes)
    val signaturePatient: ByteArray? = null,

    // Ort
    val ort: String = ""
)
