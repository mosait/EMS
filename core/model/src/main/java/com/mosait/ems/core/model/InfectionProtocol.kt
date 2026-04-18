package com.mosait.ems.core.model

data class InfectionProtocol(
    val id: Long = 0,
    val patientId: Long = 0,

    // Bekannte Infektionen des Patienten
    val bekannteInfektionen: List<String> = emptyList(),
    val infektionFreitext: String = "",

    // Schutzmaßnahmen
    val schutzHandschuhe: Boolean = false,
    val schutzMundschutz: Boolean = false,
    val schutzSchutzbrille: Boolean = false,
    val schutzSchutzkittel: Boolean = false,
    val schutzFFP2: Boolean = false,
    val schutzSonstiges: String = "",

    // Exposition / Kontamination
    val expositionStichverletzung: Boolean = false,
    val expositionSchleimhaut: Boolean = false,
    val expositionHautkontakt: Boolean = false,
    val expositionKeine: Boolean = true,

    // Desinfektion
    val fahrzeugDesinfiziert: Boolean = false,
    val geraeteDesinfiziert: Boolean = false,
    val waescheGewechselt: Boolean = false,
    val desinfektionsmittel: String = "",
    val desinfektionDurchgefuehrtVon: String = "",
    val bemerkungen: String = ""
)

object InfectionOptions {
    val BEKANNTE_INFEKTIONEN = listOf(
        "MRSA",
        "VRE",
        "ESBL",
        "3MRGN / 4MRGN",
        "Hepatitis B",
        "Hepatitis C",
        "HIV",
        "Tuberkulose",
        "COVID-19",
        "Norovirus",
        "Influenza",
        "Meningokokken",
        "Sonstiges"
    )
}
