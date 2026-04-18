package com.mosait.ems.core.model

import java.time.LocalDateTime

data class VitalSign(
    val id: Long = 0,
    val patientId: Long = 0,
    val timestamp: LocalDateTime = LocalDateTime.now(),

    val puls: Int? = null,
    val rrSystolisch: Int? = null,
    val rrDiastolisch: Int? = null,
    val spO2: Int? = null,
    val atemfrequenz: Int? = null,
    val blutzucker: Int? = null,
    val temperatur: Float? = null,
    val ekg: EkgRhythmus? = null,
    val gcs: Int? = null,
    val schmerzSkala: Int? = null,
    val bemerkung: String = ""
)
