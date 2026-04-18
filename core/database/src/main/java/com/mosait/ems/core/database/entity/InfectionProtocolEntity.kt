package com.mosait.ems.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "infection_protocols",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId", unique = true)]
)
data class InfectionProtocolEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,

    val bekannteInfektionenJson: String = "[]",
    val infektionFreitext: String = "",

    val schutzHandschuhe: Boolean = false,
    val schutzMundschutz: Boolean = false,
    val schutzSchutzbrille: Boolean = false,
    val schutzSchutzkittel: Boolean = false,
    val schutzFFP2: Boolean = false,
    val schutzSonstiges: String = "",

    val expositionStichverletzung: Boolean = false,
    val expositionSchleimhaut: Boolean = false,
    val expositionHautkontakt: Boolean = false,
    val expositionKeine: Boolean = true,

    val fahrzeugDesinfiziert: Boolean = false,
    val geraeteDesinfiziert: Boolean = false,
    val waescheGewechselt: Boolean = false,
    val desinfektionsmittel: String = "",
    val desinfektionDurchgefuehrtVon: String = "",
    val bemerkungen: String = ""
)
