package com.mosait.ems.core.model

data class Diagnosis(
    val id: Long = 0,
    val patientId: Long = 0,
    val keine: Boolean = false,
    val selectedConditions: List<String> = emptyList(),
    val sonstigesTexte: Map<String, String> = emptyMap(),
    val freitext: String = ""
)

object DiagnosisCategories {
    val ATMUNG = listOf(
        "Asthma", "COPD", "Pneumonie", "Lungenödem",
        "Pneumothorax", "Aspiration", "Pseudokrupp", "Sonstiges (Atmung)"
    )

    val HERZ_KREISLAUF = listOf(
        "ACS / Herzinfarkt", "Angina Pectoris", "Herzinsuffizienz",
        "Herzrhythmusstörung", "Hypertonie", "Hypotonie",
        "Lungenembolie", "Aortenaneurysma", "Sonstiges (Herz-Kreislauf)"
    )

    val ZNS = listOf(
        "Apoplex / Schlaganfall", "Krampfanfall / Epilepsie",
        "Synkope", "Meningitis", "SHT", "Sonstiges (ZNS)"
    )

    val ABDOMEN = listOf(
        "Akutes Abdomen", "GI-Blutung", "Ileus",
        "Appendizitis", "Gallenkolik", "Nierenkolik", "Sonstiges (Abdomen)"
    )

    val STOFFWECHSEL = listOf(
        "Diabetes / Hypoglykämie", "Diabetes / Hyperglykämie",
        "Allergische Reaktion", "Anaphylaxie", "Sonstiges (Stoffwechsel)"
    )

    val PSYCHIATRIE = listOf(
        "Erregungszustand", "Suizidalität", "Intoxikation", "Sonstiges (Psychiatrie)"
    )

    val GYNAEKOLOGIE = listOf(
        "Geburt", "Blutung in der Schwangerschaft",
        "Eklampsie", "Abort", "Sonstiges (Gynäkologie)"
    )

    val PAEDIATRIE = listOf(
        "Fieberkrampf", "Pseudokrupp", "SIDS", "Sonstiges (Pädiatrie)"
    )

    val SONSTIGES = listOf(
        "Ertrinken", "Unterkühlung", "Hitzschlag",
        "Stromunfall", "Vergiftung", "Sonstiges (Sonstiges)"
    )

    val ALL_CATEGORIES = mapOf(
        "Atmung" to ATMUNG,
        "Herz-Kreislauf" to HERZ_KREISLAUF,
        "ZNS" to ZNS,
        "Abdomen" to ABDOMEN,
        "Stoffwechsel" to STOFFWECHSEL,
        "Psychiatrie" to PSYCHIATRIE,
        "Gynäkologie" to GYNAEKOLOGIE,
        "Pädiatrie" to PAEDIATRIE,
        "Sonstiges" to SONSTIGES
    )
}
