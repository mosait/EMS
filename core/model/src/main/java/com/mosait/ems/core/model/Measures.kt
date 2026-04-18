package com.mosait.ems.core.model

data class Measures(
    val id: Long = 0,
    val patientId: Long = 0,
    val selectedMeasures: List<String> = emptyList(),
    val sauerstoffLiterProMin: Float? = null,
    val medikamente: String = "",
    val sonstige: String = "",
    val sonstigesTexte: Map<String, String> = emptyMap(),

    // Ersthelfer
    val ersthelferMassnahmen: ErsthelferMassnahmen = ErsthelferMassnahmen()
)

data class ErsthelferMassnahmen(
    val suffizient: Boolean = false,
    val insuffizient: Boolean = false,
    val aed: Boolean = false,
    val keine: Boolean = false
)

object MeasureCategories {
    val ATEMWEG = listOf(
        "Absaugen", "Guedel-Tubus", "Wendl-Tubus",
        "Larynxtubus / -maske", "Intubation", "Koniotomie", "Sonstiges (Atemweg)"
    )

    val BEATMUNG = listOf(
        "O₂-Gabe", "Beutel-Maske", "CPAP",
        "Maschinelle Beatmung", "Sonstiges (Beatmung)"
    )

    val KREISLAUF = listOf(
        "Venenzugang peripher", "Venenzugang zentral",
        "Intraossärer Zugang", "Infusion",
        "Thoraxdrainage", "Reanimation / CPR",
        "Defibrillation", "Kardioversion", "Schrittmacher", "Sonstiges (Kreislauf)"
    )

    val IMMOBILISATION = listOf(
        "HWS-Stützkragen", "Vakuummatratze", "Schaufeltrage",
        "Spineboard", "SAM-Splint / Schiene", "Beckenschlinge", "Sonstiges (Immobilisation)"
    )

    val SONSTIGES = listOf(
        "Wundversorgung", "Verbrennung kühlen",
        "Monitoring", "12-Kanal-EKG",
        "Blutstillung / Tourniquet", "Geburtshilfe",
        "Temperaturmanagement", "Sonstiges (Sonstiges)"
    )

    val ALL_CATEGORIES = mapOf(
        "Atemweg" to ATEMWEG,
        "Beatmung" to BEATMUNG,
        "Kreislauf" to KREISLAUF,
        "Immobilisation" to IMMOBILISATION,
        "Sonstiges" to SONSTIGES
    )
}
