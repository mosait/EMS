package com.mosait.ems.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Overview : Route

    @Serializable
    data class MissionDetail(val missionId: Long) : Route

    @Serializable
    data class MissionCreate(val placeholder: Boolean = false) : Route

    @Serializable
    data class MissionEdit(val missionId: Long) : Route

    @Serializable
    data class PatientDetail(val missionId: Long, val patientId: Long) : Route

    @Serializable
    data class PatientCreate(val missionId: Long) : Route

    @Serializable
    data class PatientEdit(val missionId: Long, val patientId: Long) : Route

    @Serializable
    data class InitialAssessment(val patientId: Long) : Route

    @Serializable
    data class Notfallgeschehen(val patientId: Long) : Route

    @Serializable
    data class Diagnosis(val patientId: Long) : Route

    @Serializable
    data class Injury(val patientId: Long) : Route

    @Serializable
    data class Vitals(val patientId: Long) : Route

    @Serializable
    data class Measures(val patientId: Long) : Route

    @Serializable
    data class Result(val patientId: Long) : Route

    @Serializable
    data class InfectionProtocol(val patientId: Long) : Route

    @Serializable
    data class TransportRefusal(val patientId: Long) : Route

    @Serializable
    data class Export(val missionId: Long) : Route

    @Serializable
    data object Settings : Route
}
