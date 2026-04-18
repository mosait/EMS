package com.mosait.ems.core.model

data class Injury(
    val id: Long = 0,
    val patientId: Long = 0,
    val keine: Boolean = false,
    val injuryTypes: List<InjuryType> = emptyList(),
    val bodyRegions: List<BodyRegionEntry> = emptyList(),
    val kopfHalsFreitext: String = "",
    val freitext: String = ""
)

enum class InjuryType {
    PRELLUNG,
    FRAKTUR,
    WUNDE,
    VERBRENNUNG,
    VERAETZUNG,
    AMPUTATION,
    LUXATION,
    DISTORSION,
    POLYTRAUMA,
    SONSTIGES
}

data class BodyRegionEntry(
    val region: BodyRegion,
    val severity: InjurySeverity = InjurySeverity.LEICHT,
    val side: BodySide = BodySide.MITTE
)

enum class BodyRegion {
    KOPF,
    GESICHT,
    HALS,
    BRUST,
    BAUCH,
    BECKEN,
    RUECKEN,
    WIRBELSAEULE,
    OBERARM_LINKS,
    OBERARM_RECHTS,
    UNTERARM_LINKS,
    UNTERARM_RECHTS,
    HAND_LINKS,
    HAND_RECHTS,
    OBERSCHENKEL_LINKS,
    OBERSCHENKEL_RECHTS,
    UNTERSCHENKEL_LINKS,
    UNTERSCHENKEL_RECHTS,
    FUSS_LINKS,
    FUSS_RECHTS
}

enum class InjurySeverity {
    LEICHT,
    MITTEL,
    SCHWER
}

enum class BodySide {
    LINKS,
    RECHTS,
    MITTE,
    BEIDSEITIG
}
