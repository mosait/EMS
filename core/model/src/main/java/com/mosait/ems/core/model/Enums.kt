package com.mosait.ems.core.model

enum class MissionStatus(val displayName: String) {
    DRAFT("Entwurf"),
    IN_PROGRESS("Laufend"),
    COMPLETED("Abgeschlossen"),
    EXPORTED("Exportiert")
}

enum class EinsatzArt {
    NOTFALLEINSATZ,
    KRANKENTRANSPORT,
    FEHLFAHRT,
    BEREITSCHAFT,
    SONSTIGES
}

enum class RettungsMittel {
    RTW,
    KTW,
    NEF,
    NAW,
    RTH,
    SONSTIGES
}

data class PersonalEntry(
    val name: String = "",
    val rolle: PersonalRolle = PersonalRolle.RETTUNGSSANITAETER
)

enum class PersonalRolle(val displayName: String) {
    NOTARZT("Notarzt"),
    NOTFALLSANITAETER("Notfallsanitäter"),
    RETTUNGSASSISTENT("Rettungsassistent"),
    RETTUNGSSANITAETER("Rettungssanitäter"),
    PRAKTIKANT("Praktikant"),
    SONSTIGES("Sonstiges")
}
