package com.mosait.ems.feature.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.BodyRegion
import com.mosait.ems.core.model.InjurySeverity
import com.mosait.ems.core.model.MissionStatus
import com.mosait.ems.core.model.VitalSign
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

data class ExportUiState(
    val missionSummary: String? = null,
    val patientCount: Int = 0,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportedFilePath: File? = null,
    val exportedMimeType: String = "application/pdf",
    val errorMessage: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val missionRepository: MissionRepository,
    private val patientRepository: PatientRepository,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val missionId: Long = savedStateHandle["missionId"] ?: 0L

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val mission = missionRepository.getMissionById(missionId).first()
            val patients = patientRepository.getPatientsByMission(missionId).first()
            if (mission != null) {
                val summary = buildString {
                    appendLine("Einsatz: ${mission.einsatzNummer.ifBlank { "#${mission.id}" }}")
                    appendLine("Datum: ${DateTimeUtil.formatDate(mission.einsatzDatum)}")
                    appendLine("Art: ${mission.einsatzArt.name}")
                    appendLine("Mittel: ${mission.rettungsMittel.name}")
                    if (mission.einsatzOrtStrasse.isNotBlank()) {
                        appendLine("Ort: ${mission.einsatzOrtStrasse}, ${mission.einsatzOrtPlz} ${mission.einsatzOrtOrt}")
                    }
                    if (mission.transportZiel.isNotBlank()) {
                        appendLine("Ziel: ${mission.transportZiel}")
                    }
                }
                _uiState.update {
                    it.copy(missionSummary = summary.trimEnd(), patientCount = patients.size)
                }
            }
        }
    }

    private suspend fun markAsExported() {
        val mission = missionRepository.getMissionById(missionId).first() ?: return
        if (mission.status != MissionStatus.EXPORTED) {
            missionRepository.updateMission(mission.copy(status = MissionStatus.EXPORTED))
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccess = false, errorMessage = null) }
            try {
                val mission = missionRepository.getMissionById(missionId).first()
                    ?: throw Exception("Einsatz nicht gefunden")
                val patients = patientRepository.getPatientsByMission(missionId).first()

                val document = PdfDocument()
                val pageWidth = 595 // A4
                val pageHeight = 842

                val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
                val headerPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = 0xFF1B5FA8.toInt() }
                val textPaint = Paint().apply { textSize = 11f }
                val smallPaint = Paint().apply { textSize = 9f; color = android.graphics.Color.GRAY }
                val separatorPaint = Paint().apply { color = 0xFFCCCCCC.toInt(); strokeWidth = 1f }

                var pageNumber = 1
                var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                var page = document.startPage(pageInfo)
                var canvas = page.canvas
                var y = 40f
                val marginLeft = 40f
                val lineHeight = 16f

                fun checkNewPage() {
                    if (y > pageHeight - 60) {
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f
                    }
                }

                fun drawLine(text: String, paint: Paint = textPaint, extraSpacing: Float = 0f) {
                    checkNewPage()
                    canvas.drawText(text, marginLeft, y, paint)
                    y += lineHeight + extraSpacing
                }

                fun drawSectionHeader(text: String) {
                    y += 6f
                    checkNewPage()
                    canvas.drawText(text, marginLeft, y, headerPaint)
                    y += 4f
                    canvas.drawLine(marginLeft, y, pageWidth - marginLeft, y, separatorPaint)
                    y += lineHeight
                }

                // Header
                drawLine("Einsatzprotokoll", titlePaint, 8f)
                drawLine("Erstellt: ${DateTimeUtil.formatDate(LocalDate.now())}", smallPaint, 12f)

                // Mission info
                drawSectionHeader("Einsatzdaten")
                drawLine("Einsatznummer: ${mission.einsatzNummer.ifBlank { "-" }}")
                drawLine("Datum: ${DateTimeUtil.formatDate(mission.einsatzDatum)}")
                drawLine("Art: ${mission.einsatzArt.name.replace("_", " ")}")
                drawLine("Rettungsmittel: ${mission.rettungsMittel.name}")
                if (mission.fahrzeugKennung.isNotBlank()) {
                    drawLine("Fahrzeugkennung: ${mission.fahrzeugKennung}")
                }
                if (mission.funkKennung.isNotBlank()) {
                    drawLine("Funkkennung: ${mission.funkKennung}")
                }
                if (mission.personal.isNotEmpty()) {
                    drawLine("Besatzung: ${mission.personal.joinToString(", ") { "${it.name} (${it.rolle.displayName})" }}")
                }
                if (mission.einsatzOrtStrasse.isNotBlank()) {
                    val zusatz = if (mission.einsatzOrtZusatz.isNotBlank()) " (${mission.einsatzOrtZusatz})" else ""
                    drawLine("Einsatzort: ${mission.einsatzOrtStrasse}, ${mission.einsatzOrtPlz} ${mission.einsatzOrtOrt}$zusatz")
                }
                if (mission.transportZiel.isNotBlank()) {
                    drawLine("Transportziel: ${mission.transportZiel}")
                }
                // Zeiten
                mission.zeitAlarm?.let { drawLine("Alarm: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAbfahrt?.let { drawLine("Abfahrt: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAnkunftEinsatzort?.let { drawLine("Ankunft Einsatzort: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAbfahrtEinsatzort?.let { drawLine("Abfahrt Einsatzort: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAnkunftKrankenhaus?.let { drawLine("Ankunft Krankenhaus: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitFreimeldung?.let { drawLine("Freimeldung: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitEnde?.let { drawLine("Ende: ${DateTimeUtil.formatDateTime(it)}") }
                // KM
                if (mission.kmAnfang != null || mission.kmEnde != null) {
                    val km = listOfNotNull(
                        mission.kmAnfang?.let { "Anfang: $it" },
                        mission.kmEnde?.let { "Ende: $it" }
                    ).joinToString("  ")
                    drawLine("KM-Stand: $km")
                }
                // Sondersignal
                val sondersignal = listOfNotNull(
                    if (mission.sondersignalZumEinsatz) "zum Einsatz" else null,
                    if (mission.sondersignalPatientenfahrt) "Patientenfahrt" else null
                )
                if (sondersignal.isNotEmpty()) {
                    drawLine("Sondersignal: ${sondersignal.joinToString(", ")}")
                }
                if (mission.bemerkungen.isNotBlank()) {
                    drawLine("Bemerkungen: ${mission.bemerkungen}")
                }
                y += 12f

                // Each patient
                for ((index, patient) in patients.withIndex()) {
                    drawSectionHeader("Patient ${index + 1}: ${patient.nachname}, ${patient.vorname}")
                    drawLine("Geb.: ${DateTimeUtil.formatDate(patient.geburtsdatum)}  Geschlecht: ${patient.geschlecht.name}")
                    // Adresse
                    if (patient.strasse.isNotBlank()) {
                        drawLine("Adresse: ${patient.strasse}, ${patient.plz} ${patient.ort}")
                    }
                    if (patient.telefon.isNotBlank()) {
                        drawLine("Telefon: ${patient.telefon}")
                    }
                    // Versicherung
                    if (patient.krankenkasse.isNotBlank()) {
                        drawLine("Krankenkasse: ${patient.krankenkasse}")
                    }
                    if (patient.versichertenNummer.isNotBlank()) {
                        drawLine("Vers.-Nr.: ${patient.versichertenNummer}  Status: ${patient.versichertenStatus}")
                    }

                    // Notfallgeschehen
                    val assessment = protocolRepository.getInitialAssessment(patient.id).first()
                    if (assessment != null && assessment.notfallgeschehen.isNotBlank()) {
                        y += 4f
                        drawSectionHeader("Notfallgeschehen")
                        drawLine(assessment.notfallgeschehen.take(500))
                    }

                    // Initial assessment
                    if (assessment != null) {
                        y += 4f
                        drawSectionHeader("Erstbefund")
                        drawLine("Bewusstsein: ${assessment.bewusstseinslage.name}")
                        if (assessment.bewusstseinslageText.isNotBlank()) {
                            drawLine("  ${assessment.bewusstseinslageText}")
                        }
                        // Kreislauf
                        val kreislauf = listOfNotNull(
                            if (assessment.kreislaufSchock) "Schock" else null,
                            if (assessment.kreislaufStillstand) "Stillstand" else null,
                            if (assessment.kreislaufReanimation) "Reanimation" else null
                        )
                        if (kreislauf.isNotEmpty()) {
                            drawLine("Kreislauf: ${kreislauf.joinToString(", ")}")
                        }
                        if (assessment.kreislaufSonstigesText.isNotBlank()) {
                            drawLine("Kreislauf Sonstiges: ${assessment.kreislaufSonstigesText}")
                        }
                        assessment.rrSystolisch?.let { sys ->
                            val dias = assessment.rrDiastolisch?.let { "/$it" } ?: ""
                            drawLine("RR: $sys$dias mmHg")
                        }
                        assessment.puls?.let { drawLine("Puls: $it /min") }
                        assessment.spO2?.let { drawLine("SpO2: $it %") }
                        assessment.atemfrequenz?.let { drawLine("Atemfrequenz: $it /min") }
                        assessment.blutzucker?.let { drawLine("Blutzucker: $it mg/dl") }
                        assessment.temperatur?.let { drawLine("Temperatur: $it °C") }
                        drawLine("GCS: ${assessment.gcsAugen + assessment.gcsVerbal + assessment.gcsMotorik} (A:${assessment.gcsAugen} V:${assessment.gcsVerbal} M:${assessment.gcsMotorik})")
                        drawLine("Pupille L: ${assessment.pupilleLinks.name} (Licht: ${if (assessment.pupillenLichtreaktionLinks) "+" else "-"})  R: ${assessment.pupilleRechts.name} (Licht: ${if (assessment.pupillenLichtreaktionRechts) "+" else "-"})")
                        drawLine("EKG: ${assessment.ekg.name}")
                        if (assessment.ekgSonstigesText.isNotBlank()) {
                            drawLine("  ${assessment.ekgSonstigesText}")
                        }
                        drawLine("Schmerz NRS: ${assessment.schmerzSkala}")
                        drawLine("Atmung: ${assessment.atmung.name}")
                        if (assessment.atmungSonstigesText.isNotBlank()) {
                            drawLine("  ${assessment.atmungSonstigesText}")
                        }
                    }

                    // Diagnosis
                    val diagnosis = protocolRepository.getDiagnosis(patient.id).first()
                    if (diagnosis != null && !diagnosis.keine && (diagnosis.selectedConditions.isNotEmpty() || diagnosis.freitext.isNotBlank())) {
                        y += 4f
                        drawSectionHeader("Erkrankung")
                        if (diagnosis.selectedConditions.isNotEmpty()) {
                            drawLine(diagnosis.selectedConditions.joinToString(", "))
                        }
                        diagnosis.sonstigesTexte.forEach { (key, value) ->
                            if (value.isNotBlank()) drawLine("$key: $value")
                        }
                        if (diagnosis.freitext.isNotBlank()) {
                            drawLine("Freitext: ${diagnosis.freitext}")
                        }
                    }

                    // Injury
                    val injury = protocolRepository.getInjury(patient.id).first()
                    if (injury != null && !injury.keine && (injury.injuryTypes.isNotEmpty() || injury.bodyRegions.isNotEmpty() || injury.freitext.isNotBlank())) {
                        y += 4f
                        drawSectionHeader("Verletzung")

                        // If we have body regions, render text on the left and silhouette on the right
                        if (injury.bodyRegions.isNotEmpty()) {
                            val regionSeverities = injury.bodyRegions.associate { it.region to it.severity }
                            val silBitmap = renderBodySilhouetteBitmap(regionSeverities)
                            val silWidth = 150f
                            val silHeight = silWidth * silBitmap.height / silBitmap.width

                            // Check if silhouette fits on current page
                            if (y + silHeight > pageHeight - 60) {
                                document.finishPage(page)
                                pageNumber++
                                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                                page = document.startPage(pageInfo)
                                canvas = page.canvas
                                y = 40f
                            }

                            // Draw silhouette on the right
                            val silX = pageWidth - marginLeft - silWidth
                            val silStartY = y
                            val srcRect = android.graphics.Rect(0, 0, silBitmap.width, silBitmap.height)
                            val dstRect = android.graphics.RectF(silX, silStartY, silX + silWidth, silStartY + silHeight)
                            canvas.drawBitmap(silBitmap, srcRect, dstRect, null)
                            silBitmap.recycle()

                            // Draw text on the left (limited width so it doesn't overlap)
                            val textStartY = y
                            if (injury.injuryTypes.isNotEmpty()) {
                                drawLine("Art: ${injury.injuryTypes.joinToString(", ") { it.name.replace("_", " ") }}")
                            }
                            drawLine("Betroffene Regionen:")
                            injury.bodyRegions.forEach { entry ->
                                drawLine("  • ${entry.region.name.replace("_", " ")} — ${entry.severity.name}")
                            }
                            if (injury.kopfHalsFreitext.isNotBlank()) {
                                drawLine("Kopf/Hals: ${injury.kopfHalsFreitext}")
                            }
                            if (injury.freitext.isNotBlank()) {
                                drawLine("Freitext: ${injury.freitext}")
                            }

                            // Ensure y is at least past the silhouette
                            y = maxOf(y, silStartY + silHeight + 8f)
                        } else {
                            // No body regions, just text
                            if (injury.injuryTypes.isNotEmpty()) {
                                drawLine("Art: ${injury.injuryTypes.joinToString(", ") { it.name.replace("_", " ") }}")
                            }
                            if (injury.kopfHalsFreitext.isNotBlank()) {
                                drawLine("Kopf/Hals: ${injury.kopfHalsFreitext}")
                            }
                            if (injury.freitext.isNotBlank()) {
                                drawLine("Freitext: ${injury.freitext}")
                            }
                        }
                    }

                    // Vital signs table
                    val vitalSigns = protocolRepository.getVitalSigns(patient.id).first()
                    if (vitalSigns.isNotEmpty()) {
                        y += 4f
                        drawSectionHeader("Vitalwerte-Verlauf")

                        // Table layout
                        val tablePaint = Paint().apply { textSize = 9f }
                        val tableBoldPaint = Paint().apply { textSize = 9f; isFakeBoldText = true }
                        val tableLinePaint = Paint().apply {
                            color = android.graphics.Color.LTGRAY
                            strokeWidth = 1f
                            style = Paint.Style.STROKE
                        }

                        val labelColWidth = 90f
                        val valueColWidth = 65f
                        val rowHeight = 14f
                        val tableWidth = labelColWidth + valueColWidth * vitalSigns.size
                        val tableLeft = marginLeft

                        // Check if table fits, otherwise new page
                        val labels = listOf("Zeit", "RR (mmHg)", "Puls (/min)", "SpO₂ (%)", "AF (/min)", "BZ (mg/dl)", "Temp. (°C)", "GCS", "NRS", "EKG")
                        val totalTableHeight = labels.size * rowHeight + 4f
                        if (y + totalTableHeight > pageHeight - 60) {
                            document.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = document.startPage(pageInfo)
                            canvas = page.canvas
                            y = 40f
                        }

                        val tableTop = y

                        // Draw header row
                        for ((rowIdx, label) in labels.withIndex()) {
                            val rowY = tableTop + rowIdx * rowHeight
                            // Label
                            canvas.drawText(label, tableLeft + 2f, rowY + rowHeight - 3f, if (rowIdx == 0) tableBoldPaint else tablePaint)
                            // Values
                            for ((colIdx, vital) in vitalSigns.withIndex()) {
                                val colX = tableLeft + labelColWidth + colIdx * valueColWidth
                                val text = when (rowIdx) {
                                    0 -> DateTimeUtil.formatTime(vital.timestamp)
                                    1 -> vital.rrSystolisch?.let { sys -> "$sys${vital.rrDiastolisch?.let { "/$it" } ?: ""}" } ?: ""
                                    2 -> vital.puls?.toString() ?: ""
                                    3 -> vital.spO2?.toString() ?: ""
                                    4 -> vital.atemfrequenz?.toString() ?: ""
                                    5 -> vital.blutzucker?.toString() ?: ""
                                    6 -> vital.temperatur?.toString() ?: ""
                                    7 -> vital.gcs?.toString() ?: ""
                                    8 -> vital.schmerzSkala?.toString() ?: ""
                                    9 -> vital.ekg?.name?.replace("_", " ") ?: ""
                                    else -> ""
                                }
                                canvas.drawText(text, colX + 2f, rowY + rowHeight - 3f, if (rowIdx == 0) tableBoldPaint else tablePaint)
                            }
                            // Horizontal line
                            canvas.drawLine(tableLeft, rowY + rowHeight, tableLeft + labelColWidth + vitalSigns.size * valueColWidth, rowY + rowHeight, tableLinePaint)
                        }

                        // Vertical lines
                        canvas.drawLine(tableLeft, tableTop, tableLeft, tableTop + labels.size * rowHeight, tableLinePaint)
                        canvas.drawLine(tableLeft + labelColWidth, tableTop, tableLeft + labelColWidth, tableTop + labels.size * rowHeight, tableLinePaint)
                        for (colIdx in 1..vitalSigns.size) {
                            val colX = tableLeft + labelColWidth + colIdx * valueColWidth
                            canvas.drawLine(colX, tableTop, colX, tableTop + labels.size * rowHeight, tableLinePaint)
                        }
                        // Top border
                        canvas.drawLine(tableLeft, tableTop, tableLeft + labelColWidth + vitalSigns.size * valueColWidth, tableTop, tableLinePaint)

                        y = tableTop + labels.size * rowHeight + 8f

                        // Bemerkungen below table
                        for (vital in vitalSigns) {
                            if (vital.bemerkung.isNotBlank()) {
                                drawLine("${DateTimeUtil.formatTime(vital.timestamp)}: ${vital.bemerkung}", smallPaint)
                            }
                        }
                    }

                    // Measures
                    val measures = protocolRepository.getMeasures(patient.id).first()
                    if (measures != null) {
                        val hasMeasures = measures.selectedMeasures.isNotEmpty() || measures.medikamente.isNotBlank() || measures.sonstige.isNotBlank()
                        if (hasMeasures) {
                            y += 4f
                            drawSectionHeader("Maßnahmen")
                            if (measures.selectedMeasures.isNotEmpty()) {
                                drawLine(measures.selectedMeasures.joinToString(", "))
                            }
                            measures.sauerstoffLiterProMin?.let { drawLine("Sauerstoff: $it l/min") }
                            measures.medikamente.takeIf { it.isNotBlank() }?.let { drawLine("Medikamente: $it") }
                            measures.sonstige.takeIf { it.isNotBlank() }?.let { drawLine("Sonstige: $it") }
                            measures.sonstigesTexte.forEach { (key, value) ->
                                if (value.isNotBlank()) drawLine("$key: $value")
                            }
                            val ersthelfer = measures.ersthelferMassnahmen
                            val ersthelferInfo = listOfNotNull(
                                if (ersthelfer.suffizient) "suffizient" else null,
                                if (ersthelfer.insuffizient) "insuffizient" else null,
                                if (ersthelfer.aed) "AED" else null,
                                if (ersthelfer.keine) "keine" else null
                            )
                            if (ersthelferInfo.isNotEmpty()) {
                                drawLine("Ersthelfer: ${ersthelferInfo.joinToString(", ")}")
                            }
                        }
                    }

                    // Result
                    val result = protocolRepository.getMissionResult(patient.id).first()
                    if (result != null) {
                        y += 4f
                        drawSectionHeader("Ergebnis / Übergabe")
                        val zustand = listOfNotNull(
                            if (result.zustandVerbessert) "verbessert" else null,
                            if (result.zustandUnveraendert) "unverändert" else null,
                            if (result.zustandVerschlechtert) "verschlechtert" else null
                        )
                        if (zustand.isNotEmpty()) drawLine("Zustand: ${zustand.joinToString(", ")}")
                        // Transport
                        val transport = listOfNotNull(
                            if (result.transportNichtErforderlich) "nicht erforderlich" else null,
                            if (result.patientLehntTransportAb) "Patient lehnt ab" else null
                        )
                        if (transport.isNotEmpty()) drawLine("Transport: ${transport.joinToString(", ")}")
                        // Notarzt
                        val notarzt = listOfNotNull(
                            if (result.notarztNachgefordert) "nachgefordert" else null,
                            if (result.notarztAbbestellt) "abbestellt" else null
                        )
                        if (notarzt.isNotEmpty()) drawLine("Notarzt: ${notarzt.joinToString(", ")}")
                        if (result.hausarztInformiert) drawLine("Hausarzt informiert: Ja")
                        if (result.todAmNotfallort) drawLine("Tod am Notfallort: Ja")
                        if (result.todWaehrendTransport) drawLine("Tod während Transport: Ja")
                        if (result.sonstigesFreitext.isNotBlank()) drawLine("Sonstiges: ${result.sonstigesFreitext}")
                        result.nacaScore?.let { drawLine("NACA: $it") }
                        if (result.uebergabeAn.isNotBlank()) drawLine("Übergabe an: ${result.uebergabeAn}")
                        result.uebergabeZeit?.let { drawLine("Übergabezeit: ${DateTimeUtil.formatDateTime(it)}") }
                        if (result.wertsachen.isNotBlank()) drawLine("Wertsachen: ${result.wertsachen}")
                        if (result.verlaufsbeschreibung.isNotBlank()) {
                            drawLine("Verlauf: ${result.verlaufsbeschreibung}")
                        }
                    }

                    // Infection Protocol
                    val infection = protocolRepository.getInfectionProtocol(patient.id).first()
                    if (infection != null) {
                        val hasInfectionData = infection.bekannteInfektionen.isNotEmpty() ||
                            infection.infektionFreitext.isNotBlank() ||
                            infection.fahrzeugDesinfiziert || infection.geraeteDesinfiziert || infection.waescheGewechselt ||
                            !infection.expositionKeine
                        if (hasInfectionData) {
                            y += 4f
                            drawSectionHeader("Infektionsprotokoll")
                            if (infection.bekannteInfektionen.isNotEmpty()) {
                                drawLine("Infektionen: ${infection.bekannteInfektionen.joinToString(", ")}")
                            }
                            if (infection.infektionFreitext.isNotBlank()) {
                                drawLine("Sonstige: ${infection.infektionFreitext}")
                            }
                            val schutz = listOfNotNull(
                                if (infection.schutzHandschuhe) "Handschuhe" else null,
                                if (infection.schutzMundschutz) "Mundschutz" else null,
                                if (infection.schutzFFP2) "FFP2" else null,
                                if (infection.schutzSchutzbrille) "Schutzbrille" else null,
                                if (infection.schutzSchutzkittel) "Schutzkittel" else null
                            )
                            if (schutz.isNotEmpty()) drawLine("Schutzmaßnahmen: ${schutz.joinToString(", ")}")
                            if (!infection.expositionKeine) {
                                val exposition = listOfNotNull(
                                    if (infection.expositionStichverletzung) "Stich-/Schnittverletzung" else null,
                                    if (infection.expositionSchleimhaut) "Schleimhautkontakt" else null,
                                    if (infection.expositionHautkontakt) "Hautkontakt" else null
                                )
                                if (exposition.isNotEmpty()) drawLine("Exposition: ${exposition.joinToString(", ")}")
                            }
                            val desinfektion = listOfNotNull(
                                if (infection.fahrzeugDesinfiziert) "Fahrzeug" else null,
                                if (infection.geraeteDesinfiziert) "Geräte" else null,
                                if (infection.waescheGewechselt) "Wäsche gewechselt" else null
                            )
                            if (desinfektion.isNotEmpty()) drawLine("Desinfektion: ${desinfektion.joinToString(", ")}")
                            if (infection.bemerkungen.isNotBlank()) drawLine("Bemerkungen: ${infection.bemerkungen}")
                        }
                    }

                    // Transport Refusal
                    val refusal = protocolRepository.getTransportRefusal(patient.id).first()
                    if (refusal != null && refusal.enabled) {
                        // Start new page for the formal document
                        document.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f

                        drawLine("Transportverweigerungserklärung", titlePaint, 4f)
                        drawLine("Refusal of transportation by ambulance car or of medical treatment", smallPaint, 8f)

                        drawLine("Herr/Frau: ${refusal.patientName}", textPaint, 2f)
                        drawLine("Geb. am: ${refusal.geburtsdatum}    Geb. in: ${refusal.geburtsort}", textPaint, 6f)

                        val datumStr = DateTimeUtil.formatDate(refusal.datum)
                        val uhrzeitStr = refusal.uhrzeit?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                        drawLine("Hiermit erkläre ich, dass ich heute, am $datumStr um $uhrzeitStr", textPaint, 2f)
                        drawLine("vom Rettungsdienst / Notarztdienst über meine Erkrankung bzw. Verletzung", textPaint, 2f)
                        drawLine("und deren Konsequenzen aufgeklärt worden bin und", textPaint, 4f)

                        val verweigerung = listOfNotNull(
                            if (refusal.lehntBehandlungAb) "☒ eine Behandlung" else "☐ eine Behandlung",
                            if (refusal.lehntTransportAb) "☒ die Beförderung in ein Krankenhaus" else "☐ die Beförderung in ein Krankenhaus"
                        )
                        drawLine(verweigerung.joinToString("    "), textPaint, 4f)

                        drawLine("entgegen der Belehrung ablehne. Für hieraus entstandene Schäden trage ich", textPaint, 2f)
                        drawLine("selbst die Verantwortung. Ich wurde darüber informiert, dass ich im Nachhinein", textPaint, 2f)
                        drawLine("keinerlei Ersatzansprüche wegen dieser nicht ausgeführten Krankenbeförderung /", textPaint, 2f)
                        drawLine("Behandlung und den sich evtl. daraus ergebenden gesundheitlichen Schäden", textPaint, 2f)
                        drawLine("geltend machen kann.", textPaint, 6f)

                        if (refusal.nichtAuszuschliessendeErkrankungen.isNotBlank()) {
                            drawLine("Ohne klinische Abklärung nicht auszuschließen:", textPaint, 2f)
                            drawLine(refusal.nichtAuszuschliessendeErkrankungen, textPaint, 6f)
                        }

                        if (refusal.moeglicheFolgen.isNotBlank()) {
                            drawLine("Mögliche Folgen der Transportverweigerung:", textPaint, 2f)
                            drawLine(refusal.moeglicheFolgen, textPaint, 6f)
                        }

                        y += 8f
                        drawLine("o.g. Erklärung habe ich zur Kenntnis genommen und verstanden:", textPaint, 2f)
                        drawLine("Name Patient/Patientin: ${refusal.patientName}", textPaint, 2f)

                        // Draw patient signature
                        refusal.signaturePatient?.let { sigBytes ->
                            try {
                                val sigBitmap = BitmapFactory.decodeByteArray(sigBytes, 0, sigBytes.size)
                                if (sigBitmap != null) {
                                    val sigWidth = 200f
                                    val sigHeight = sigWidth * sigBitmap.height / sigBitmap.width
                                    val srcRect = android.graphics.Rect(0, 0, sigBitmap.width, sigBitmap.height)
                                    val dstRect = android.graphics.RectF(40f, y, 40f + sigWidth, y + sigHeight)
                                    canvas.drawBitmap(sigBitmap, srcRect, dstRect, null)
                                    y += sigHeight + 4f
                                    sigBitmap.recycle()
                                }
                            } catch (_: Exception) { }
                        }
                        y += 4f

                        drawLine("o.g. Patient/Patientin wurde in meinem Beisein aufgeklärt:", textPaint, 2f)
                        drawLine("Name Zeuge/Angehöriger: ${refusal.nameZeugeAngehoeriger}", textPaint, 2f)
                        drawLine("Adresse: ${refusal.adresseZeugeAngehoeriger}", textPaint, 8f)

                        drawLine("o.g. Patient/Patientin wurde in meinem Beisein aufgeklärt:", textPaint, 2f)
                        drawLine("Name Zeuge/Rettungsdienst: ${refusal.nameZeugeRettungsdienst}", textPaint, 8f)

                        drawLine("Die Aufklärung des o.g. Patienten/Patientin erfolgte durch:", textPaint, 2f)
                        drawLine("Name Rettungsdienst/Notarztdienst: ${refusal.nameRettungsdienstNotarzt}", textPaint, 12f)

                        drawLine("Ort: ${refusal.ort}    Datum: $datumStr    Uhrzeit: $uhrzeitStr", textPaint, 2f)
                    }

                    y += 16f
                }

                document.finishPage(page)

                // Save file
                val exportDir = File(context.getExternalFilesDir(null), "exports")
                exportDir.mkdirs()
                val fileName = "Einsatz_${mission.einsatzNummer.ifBlank { mission.id.toString() }}_${System.currentTimeMillis()}.pdf"
                val file = File(exportDir, fileName)
                FileOutputStream(file).use { document.writeTo(it) }
                document.close()

                markAsExported()

                _uiState.update {
                    it.copy(isExporting = false, exportSuccess = true, exportedFilePath = file, exportedMimeType = "application/pdf")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, errorMessage = "Export fehlgeschlagen: ${e.message}")
                }
            }
        }
    }

    fun exportDocx() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccess = false, errorMessage = null) }
            try {
                val mission = missionRepository.getMissionById(missionId).first()
                    ?: throw Exception("Einsatz nicht gefunden")
                val patients = patientRepository.getPatientsByMission(missionId).first()

                // Build document content
                val bodyXml = StringBuilder()
                val docxImages = mutableListOf<Pair<String, ByteArray>>()

                fun addParagraph(text: String, bold: Boolean = false, fontSize: Int = 22, spacingAfter: Int = 100) {
                    bodyXml.append("""<w:p><w:pPr><w:spacing w:after="$spacingAfter"/></w:pPr><w:r><w:rPr>""")
                    if (bold) bodyXml.append("<w:b/>")
                    bodyXml.append("""<w:sz w:val="$fontSize"/><w:szCs w:val="$fontSize"/>""")
                    bodyXml.append("</w:rPr><w:t xml:space=\"preserve\">")
                    bodyXml.append(escapeXml(text))
                    bodyXml.append("</w:t></w:r></w:p>")
                }

                // Header
                addParagraph("Einsatzprotokoll", bold = true, fontSize = 36, spacingAfter = 200)
                addParagraph("Erstellt: ${DateTimeUtil.formatDate(LocalDate.now())}", fontSize = 18)

                // Mission info
                addParagraph("Einsatzdaten", bold = true, fontSize = 28, spacingAfter = 100)
                addParagraph("Einsatznummer: ${mission.einsatzNummer.ifBlank { "-" }}")
                addParagraph("Datum: ${DateTimeUtil.formatDate(mission.einsatzDatum)}")
                addParagraph("Art: ${mission.einsatzArt.name.replace("_", " ")}")
                addParagraph("Rettungsmittel: ${mission.rettungsMittel.name}")
                if (mission.fahrzeugKennung.isNotBlank()) {
                    addParagraph("Fahrzeugkennung: ${mission.fahrzeugKennung}")
                }
                if (mission.funkKennung.isNotBlank()) {
                    addParagraph("Funkkennung: ${mission.funkKennung}")
                }
                if (mission.personal.isNotEmpty()) {
                    addParagraph("Besatzung: ${mission.personal.joinToString(", ") { "${it.name} (${it.rolle.displayName})" }}")
                }
                if (mission.einsatzOrtStrasse.isNotBlank()) {
                    val zusatz = if (mission.einsatzOrtZusatz.isNotBlank()) " (${mission.einsatzOrtZusatz})" else ""
                    addParagraph("Einsatzort: ${mission.einsatzOrtStrasse}, ${mission.einsatzOrtPlz} ${mission.einsatzOrtOrt}$zusatz")
                }
                if (mission.transportZiel.isNotBlank()) {
                    addParagraph("Transportziel: ${mission.transportZiel}")
                }
                mission.zeitAlarm?.let { addParagraph("Alarm: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAbfahrt?.let { addParagraph("Abfahrt: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAnkunftEinsatzort?.let { addParagraph("Ankunft Einsatzort: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAbfahrtEinsatzort?.let { addParagraph("Abfahrt Einsatzort: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitAnkunftKrankenhaus?.let { addParagraph("Ankunft Krankenhaus: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitFreimeldung?.let { addParagraph("Freimeldung: ${DateTimeUtil.formatDateTime(it)}") }
                mission.zeitEnde?.let { addParagraph("Ende: ${DateTimeUtil.formatDateTime(it)}") }
                if (mission.kmAnfang != null || mission.kmEnde != null) {
                    val km = listOfNotNull(
                        mission.kmAnfang?.let { "Anfang: $it" },
                        mission.kmEnde?.let { "Ende: $it" }
                    ).joinToString("  ")
                    addParagraph("KM-Stand: $km")
                }
                val docxSondersignal = listOfNotNull(
                    if (mission.sondersignalZumEinsatz) "zum Einsatz" else null,
                    if (mission.sondersignalPatientenfahrt) "Patientenfahrt" else null
                )
                if (docxSondersignal.isNotEmpty()) {
                    addParagraph("Sondersignal: ${docxSondersignal.joinToString(", ")}")
                }
                if (mission.bemerkungen.isNotBlank()) {
                    addParagraph("Bemerkungen: ${mission.bemerkungen}")
                }

                // Each patient
                for ((index, patient) in patients.withIndex()) {
                    addParagraph("")
                    addParagraph("Patient ${index + 1}: ${patient.nachname}, ${patient.vorname}", bold = true, fontSize = 28)
                    addParagraph("Geb.: ${DateTimeUtil.formatDate(patient.geburtsdatum)}  Geschlecht: ${patient.geschlecht.name}")
                    if (patient.strasse.isNotBlank()) {
                        addParagraph("Adresse: ${patient.strasse}, ${patient.plz} ${patient.ort}")
                    }
                    if (patient.telefon.isNotBlank()) {
                        addParagraph("Telefon: ${patient.telefon}")
                    }
                    if (patient.krankenkasse.isNotBlank()) {
                        addParagraph("Krankenkasse: ${patient.krankenkasse}")
                    }
                    if (patient.versichertenNummer.isNotBlank()) {
                        addParagraph("Vers.-Nr.: ${patient.versichertenNummer}  Status: ${patient.versichertenStatus}")
                    }

                    val assessment = protocolRepository.getInitialAssessment(patient.id).first()
                    if (assessment != null && assessment.notfallgeschehen.isNotBlank()) {
                        addParagraph("Notfallgeschehen", bold = true, fontSize = 24)
                        addParagraph(assessment.notfallgeschehen)
                    }

                    if (assessment != null) {
                        addParagraph("Erstbefund", bold = true, fontSize = 24)
                        addParagraph("Bewusstsein: ${assessment.bewusstseinslage.name}")
                        if (assessment.bewusstseinslageText.isNotBlank()) {
                            addParagraph("  ${assessment.bewusstseinslageText}")
                        }
                        val kreislauf = listOfNotNull(
                            if (assessment.kreislaufSchock) "Schock" else null,
                            if (assessment.kreislaufStillstand) "Stillstand" else null,
                            if (assessment.kreislaufReanimation) "Reanimation" else null
                        )
                        if (kreislauf.isNotEmpty()) {
                            addParagraph("Kreislauf: ${kreislauf.joinToString(", ")}")
                        }
                        if (assessment.kreislaufSonstigesText.isNotBlank()) {
                            addParagraph("Kreislauf Sonstiges: ${assessment.kreislaufSonstigesText}")
                        }
                        assessment.rrSystolisch?.let { sys ->
                            val dias = assessment.rrDiastolisch?.let { "/$it" } ?: ""
                            addParagraph("RR: $sys$dias mmHg")
                        }
                        assessment.puls?.let { addParagraph("Puls: $it /min") }
                        assessment.spO2?.let { addParagraph("SpO2: $it %") }
                        assessment.atemfrequenz?.let { addParagraph("Atemfrequenz: $it /min") }
                        assessment.blutzucker?.let { addParagraph("Blutzucker: $it mg/dl") }
                        assessment.temperatur?.let { addParagraph("Temperatur: $it °C") }
                        addParagraph("GCS: ${assessment.gcsAugen + assessment.gcsVerbal + assessment.gcsMotorik} (A:${assessment.gcsAugen} V:${assessment.gcsVerbal} M:${assessment.gcsMotorik})")
                        addParagraph("Pupille L: ${assessment.pupilleLinks.name} (Licht: ${if (assessment.pupillenLichtreaktionLinks) "+" else "-"})  R: ${assessment.pupilleRechts.name} (Licht: ${if (assessment.pupillenLichtreaktionRechts) "+" else "-"})")
                        addParagraph("EKG: ${assessment.ekg.name}")
                        if (assessment.ekgSonstigesText.isNotBlank()) {
                            addParagraph("  ${assessment.ekgSonstigesText}")
                        }
                        addParagraph("Schmerz NRS: ${assessment.schmerzSkala}")
                        addParagraph("Atmung: ${assessment.atmung.name}")
                        if (assessment.atmungSonstigesText.isNotBlank()) {
                            addParagraph("  ${assessment.atmungSonstigesText}")
                        }
                    }

                    val diagnosis = protocolRepository.getDiagnosis(patient.id).first()
                    if (diagnosis != null && !diagnosis.keine && (diagnosis.selectedConditions.isNotEmpty() || diagnosis.freitext.isNotBlank())) {
                        addParagraph("Erkrankung", bold = true, fontSize = 24)
                        if (diagnosis.selectedConditions.isNotEmpty()) {
                            addParagraph(diagnosis.selectedConditions.joinToString(", "))
                        }
                        diagnosis.sonstigesTexte.forEach { (key, value) ->
                            if (value.isNotBlank()) addParagraph("$key: $value")
                        }
                        if (diagnosis.freitext.isNotBlank()) {
                            addParagraph("Freitext: ${diagnosis.freitext}")
                        }
                    }

                    val injury = protocolRepository.getInjury(patient.id).first()
                    if (injury != null && !injury.keine && (injury.injuryTypes.isNotEmpty() || injury.bodyRegions.isNotEmpty() || injury.freitext.isNotBlank())) {
                        addParagraph("Verletzung", bold = true, fontSize = 24)

                        // Build text content for injury
                        val injuryTextXml = StringBuilder()
                        fun addInjuryParagraph(text: String) {
                            injuryTextXml.append("""<w:p><w:pPr><w:spacing w:after="100"/></w:pPr><w:r><w:rPr><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr><w:t xml:space="preserve">${escapeXml(text)}</w:t></w:r></w:p>""")
                        }

                        if (injury.injuryTypes.isNotEmpty()) {
                            addInjuryParagraph("Art: ${injury.injuryTypes.joinToString(", ") { it.name.replace("_", " ") }}")
                        }
                        if (injury.bodyRegions.isNotEmpty()) {
                            addInjuryParagraph("Betroffene Regionen:")
                            injury.bodyRegions.forEach { entry ->
                                addInjuryParagraph("  • ${entry.region.name.replace("_", " ")} — ${entry.severity.name}")
                            }
                        }
                        if (injury.kopfHalsFreitext.isNotBlank()) {
                            addInjuryParagraph("Kopf/Hals: ${injury.kopfHalsFreitext}")
                        }
                        if (injury.freitext.isNotBlank()) {
                            addInjuryParagraph("Freitext: ${injury.freitext}")
                        }

                        // Body silhouette as embedded image in right column
                        if (injury.bodyRegions.isNotEmpty()) {
                            val regionSeverities = injury.bodyRegions.associate { it.region to it.severity }
                            val silBitmap = renderBodySilhouetteBitmap(regionSeverities)
                            val baos = ByteArrayOutputStream()
                            silBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                            val silBytes = baos.toByteArray()
                            silBitmap.recycle()

                            val imgName = "body_silhouette_${patient.id}.png"
                            val rId = "rIdBody${docxImages.size + 1}"
                            docxImages.add(Pair("$rId|$imgName", silBytes))
                            val imgWidthEmu = 2000000L
                            val imgHeightEmu = imgWidthEmu * 450 / 250

                            // 2-column table: text left, body silhouette right
                            bodyXml.append("""<w:tbl><w:tblPr><w:tblW w:w="0" w:type="auto"/><w:tblBorders><w:top w:val="none" w:sz="0" w:space="0" w:color="auto"/><w:left w:val="none" w:sz="0" w:space="0" w:color="auto"/><w:bottom w:val="none" w:sz="0" w:space="0" w:color="auto"/><w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/><w:insideH w:val="none" w:sz="0" w:space="0" w:color="auto"/><w:insideV w:val="none" w:sz="0" w:space="0" w:color="auto"/></w:tblBorders></w:tblPr><w:tr>""")
                            // Left cell: text
                            bodyXml.append("""<w:tc><w:tcPr><w:tcW w:w="6000" w:type="dxa"/></w:tcPr>$injuryTextXml</w:tc>""")
                            // Right cell: image
                            bodyXml.append("""<w:tc><w:tcPr><w:tcW w:w="4000" w:type="dxa"/><w:vAlign w:val="center"/></w:tcPr><w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"><wp:extent cx="$imgWidthEmu" cy="$imgHeightEmu"/><wp:docPr id="${docxImages.size + 100}" name="BodySilhouette"/><a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:nvPicPr><pic:cNvPr id="0" name="$imgName"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="$rId" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$imgWidthEmu" cy="$imgHeightEmu"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p></w:tc>""")
                            bodyXml.append("</w:tr></w:tbl>")
                        } else {
                            // No body regions, just append the text paragraphs directly
                            bodyXml.append(injuryTextXml)
                        }
                    }

                    val vitalSigns = protocolRepository.getVitalSigns(patient.id).first()
                    if (vitalSigns.isNotEmpty()) {
                        addParagraph("Vitalwerte-Verlauf", bold = true, fontSize = 24)

                        // Build OOXML table
                        val vitalLabels = listOf("Zeit", "RR (mmHg)", "Puls (/min)", "SpO₂ (%)", "AF (/min)", "BZ (mg/dl)", "Temp. (°C)", "GCS", "NRS", "EKG")
                        bodyXml.append("""<w:tbl><w:tblPr><w:tblStyle w:val="TableGrid"/><w:tblW w:w="0" w:type="auto"/><w:tblBorders><w:top w:val="single" w:sz="4" w:space="0" w:color="999999"/><w:left w:val="single" w:sz="4" w:space="0" w:color="999999"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="999999"/><w:right w:val="single" w:sz="4" w:space="0" w:color="999999"/><w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/></w:tblBorders></w:tblPr>""")

                        for ((rowIdx, label) in vitalLabels.withIndex()) {
                            bodyXml.append("<w:tr>")
                            // Label cell (bold, shaded for header)
                            val cellShading = if (rowIdx == 0) """<w:shd w:val="clear" w:color="auto" w:fill="E8E8E8"/>""" else ""
                            bodyXml.append("""<w:tc><w:tcPr><w:tcW w:w="2000" w:type="dxa"/>$cellShading</w:tcPr><w:p><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:szCs w:val="18"/></w:rPr><w:t xml:space="preserve">${escapeXml(label)}</w:t></w:r></w:p></w:tc>""")
                            // Value cells
                            for (vital in vitalSigns) {
                                val text = when (rowIdx) {
                                    0 -> DateTimeUtil.formatTime(vital.timestamp)
                                    1 -> vital.rrSystolisch?.let { sys -> "$sys${vital.rrDiastolisch?.let { "/$it" } ?: ""}" } ?: "-"
                                    2 -> vital.puls?.toString() ?: "-"
                                    3 -> vital.spO2?.toString() ?: "-"
                                    4 -> vital.atemfrequenz?.toString() ?: "-"
                                    5 -> vital.blutzucker?.toString() ?: "-"
                                    6 -> vital.temperatur?.toString() ?: "-"
                                    7 -> vital.gcs?.toString() ?: "-"
                                    8 -> vital.schmerzSkala?.toString() ?: "-"
                                    9 -> vital.ekg?.name?.replace("_", " ") ?: "-"
                                    else -> ""
                                }
                                val valShading = if (rowIdx == 0) """<w:shd w:val="clear" w:color="auto" w:fill="E8E8E8"/>""" else ""
                                val valBold = if (rowIdx == 0) "<w:b/>" else ""
                                bodyXml.append("""<w:tc><w:tcPr><w:tcW w:w="1200" w:type="dxa"/>$valShading</w:tcPr><w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:r><w:rPr>$valBold<w:sz w:val="18"/><w:szCs w:val="18"/></w:rPr><w:t xml:space="preserve">${escapeXml(text)}</w:t></w:r></w:p></w:tc>""")
                            }
                            bodyXml.append("</w:tr>")
                        }
                        bodyXml.append("</w:tbl>")

                        // Bemerkungen
                        for (vital in vitalSigns) {
                            if (vital.bemerkung.isNotBlank()) {
                                addParagraph("${DateTimeUtil.formatTime(vital.timestamp)}: ${vital.bemerkung}", fontSize = 18)
                            }
                        }
                    }

                    val measures = protocolRepository.getMeasures(patient.id).first()
                    if (measures != null) {
                        val hasMeasures = measures.selectedMeasures.isNotEmpty() || measures.medikamente.isNotBlank() || measures.sonstige.isNotBlank()
                        if (hasMeasures) {
                            addParagraph("Maßnahmen", bold = true, fontSize = 24)
                            if (measures.selectedMeasures.isNotEmpty()) {
                                addParagraph(measures.selectedMeasures.joinToString(", "))
                            }
                            measures.sauerstoffLiterProMin?.let { addParagraph("Sauerstoff: $it l/min") }
                            measures.medikamente.takeIf { it.isNotBlank() }?.let { addParagraph("Medikamente: $it") }
                            measures.sonstige.takeIf { it.isNotBlank() }?.let { addParagraph("Sonstige: $it") }
                            measures.sonstigesTexte.forEach { (key, value) ->
                                if (value.isNotBlank()) addParagraph("$key: $value")
                            }
                            val ersthelfer = measures.ersthelferMassnahmen
                            val ersthelferInfo = listOfNotNull(
                                if (ersthelfer.suffizient) "suffizient" else null,
                                if (ersthelfer.insuffizient) "insuffizient" else null,
                                if (ersthelfer.aed) "AED" else null,
                                if (ersthelfer.keine) "keine" else null
                            )
                            if (ersthelferInfo.isNotEmpty()) {
                                addParagraph("Ersthelfer: ${ersthelferInfo.joinToString(", ")}")
                            }
                        }
                    }

                    val result = protocolRepository.getMissionResult(patient.id).first()
                    if (result != null) {
                        addParagraph("Ergebnis / Übergabe", bold = true, fontSize = 24)
                        val zustand = listOfNotNull(
                            if (result.zustandVerbessert) "verbessert" else null,
                            if (result.zustandUnveraendert) "unverändert" else null,
                            if (result.zustandVerschlechtert) "verschlechtert" else null
                        )
                        if (zustand.isNotEmpty()) addParagraph("Zustand: ${zustand.joinToString(", ")}")
                        val transport = listOfNotNull(
                            if (result.transportNichtErforderlich) "nicht erforderlich" else null,
                            if (result.patientLehntTransportAb) "Patient lehnt ab" else null
                        )
                        if (transport.isNotEmpty()) addParagraph("Transport: ${transport.joinToString(", ")}")
                        val notarzt = listOfNotNull(
                            if (result.notarztNachgefordert) "nachgefordert" else null,
                            if (result.notarztAbbestellt) "abbestellt" else null
                        )
                        if (notarzt.isNotEmpty()) addParagraph("Notarzt: ${notarzt.joinToString(", ")}")
                        if (result.hausarztInformiert) addParagraph("Hausarzt informiert: Ja")
                        if (result.todAmNotfallort) addParagraph("Tod am Notfallort: Ja")
                        if (result.todWaehrendTransport) addParagraph("Tod während Transport: Ja")
                        if (result.sonstigesFreitext.isNotBlank()) addParagraph("Sonstiges: ${result.sonstigesFreitext}")
                        result.nacaScore?.let { addParagraph("NACA: $it") }
                        if (result.uebergabeAn.isNotBlank()) addParagraph("Übergabe an: ${result.uebergabeAn}")
                        result.uebergabeZeit?.let { addParagraph("Übergabezeit: ${DateTimeUtil.formatDateTime(it)}") }
                        if (result.wertsachen.isNotBlank()) addParagraph("Wertsachen: ${result.wertsachen}")
                        if (result.verlaufsbeschreibung.isNotBlank()) {
                            addParagraph("Verlauf: ${result.verlaufsbeschreibung}")
                        }
                    }

                    val infection = protocolRepository.getInfectionProtocol(patient.id).first()
                    if (infection != null) {
                        val hasInfectionData = infection.bekannteInfektionen.isNotEmpty() ||
                            infection.infektionFreitext.isNotBlank() ||
                            infection.fahrzeugDesinfiziert || infection.geraeteDesinfiziert || infection.waescheGewechselt ||
                            !infection.expositionKeine
                        if (hasInfectionData) {
                            addParagraph("Infektionsprotokoll", bold = true, fontSize = 24)
                            if (infection.bekannteInfektionen.isNotEmpty()) {
                                addParagraph("Infektionen: ${infection.bekannteInfektionen.joinToString(", ")}")
                            }
                            if (infection.infektionFreitext.isNotBlank()) {
                                addParagraph("Sonstige: ${infection.infektionFreitext}")
                            }
                            val schutz = listOfNotNull(
                                if (infection.schutzHandschuhe) "Handschuhe" else null,
                                if (infection.schutzMundschutz) "Mundschutz" else null,
                                if (infection.schutzFFP2) "FFP2" else null,
                                if (infection.schutzSchutzbrille) "Schutzbrille" else null,
                                if (infection.schutzSchutzkittel) "Schutzkittel" else null
                            )
                            if (schutz.isNotEmpty()) addParagraph("Schutzmaßnahmen: ${schutz.joinToString(", ")}")
                            if (!infection.expositionKeine) {
                                val exposition = listOfNotNull(
                                    if (infection.expositionStichverletzung) "Stich-/Schnittverletzung" else null,
                                    if (infection.expositionSchleimhaut) "Schleimhautkontakt" else null,
                                    if (infection.expositionHautkontakt) "Hautkontakt" else null
                                )
                                if (exposition.isNotEmpty()) addParagraph("Exposition: ${exposition.joinToString(", ")}")
                            }
                            val desinfektion = listOfNotNull(
                                if (infection.fahrzeugDesinfiziert) "Fahrzeug" else null,
                                if (infection.geraeteDesinfiziert) "Geräte" else null,
                                if (infection.waescheGewechselt) "Wäsche gewechselt" else null
                            )
                            if (desinfektion.isNotEmpty()) addParagraph("Desinfektion: ${desinfektion.joinToString(", ")}")
                            if (infection.bemerkungen.isNotBlank()) addParagraph("Bemerkungen: ${infection.bemerkungen}")
                        }
                    }

                    // Transport Refusal
                    val docxRefusal = protocolRepository.getTransportRefusal(patient.id).first()
                    if (docxRefusal != null && docxRefusal.enabled) {
                        addParagraph("")
                        addParagraph("Transportverweigerungserklärung", bold = true, fontSize = 36, spacingAfter = 100)
                        addParagraph("Refusal of transportation by ambulance car or of medical treatment", fontSize = 18)
                        addParagraph("")
                        addParagraph("Herr/Frau: ${docxRefusal.patientName}")
                        addParagraph("Geb. am: ${docxRefusal.geburtsdatum}    Geb. in: ${docxRefusal.geburtsort}")
                        addParagraph("")

                        val docxDatumStr = DateTimeUtil.formatDate(docxRefusal.datum)
                        val docxUhrzeitStr = docxRefusal.uhrzeit?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
                        addParagraph("Hiermit erkläre ich, dass ich heute, am $docxDatumStr um $docxUhrzeitStr vom Rettungsdienst / Notarztdienst über meine Erkrankung bzw. Verletzung und deren Konsequenzen aufgeklärt worden bin und")

                        val docxVerweigerung = listOfNotNull(
                            if (docxRefusal.lehntBehandlungAb) "☒ eine Behandlung" else "☐ eine Behandlung",
                            if (docxRefusal.lehntTransportAb) "☒ die Beförderung in ein Krankenhaus" else "☐ die Beförderung in ein Krankenhaus"
                        )
                        addParagraph(docxVerweigerung.joinToString("    "))

                        addParagraph("entgegen der Belehrung ablehne. Für hieraus entstandene Schäden trage ich selbst die Verantwortung. Ich wurde darüber informiert, dass ich im Nachhinein keinerlei Ersatzansprüche wegen dieser nicht ausgeführten Krankenbeförderung / Behandlung und den sich evtl. daraus ergebenden gesundheitlichen Schäden geltend machen kann.")

                        if (docxRefusal.nichtAuszuschliessendeErkrankungen.isNotBlank()) {
                            addParagraph("")
                            addParagraph("Ohne klinische Abklärung sind folgende Verletzungen bzw. Erkrankungen nicht auszuschließen:")
                            addParagraph(docxRefusal.nichtAuszuschliessendeErkrankungen)
                        }

                        if (docxRefusal.moeglicheFolgen.isNotBlank()) {
                            addParagraph("")
                            addParagraph("Mögliche Folgen der Transportverweigerung sind:")
                            addParagraph(docxRefusal.moeglicheFolgen)
                        }

                        addParagraph("")
                        addParagraph("o.g. Erklärung habe ich zur Kenntnis genommen und verstanden:")
                        addParagraph("Name Patient/Patientin: ${docxRefusal.patientName}")

                        // Inline patient signature image
                        docxRefusal.signaturePatient?.let { sigBytes ->
                            val imgName = "signature_patient.png"
                            val rId = "rIdSig${docxImages.size + 1}"
                            docxImages.add(Triple(imgName, sigBytes, rId).let { Pair(it.third + "|" + it.first, it.second) })
                            // Decode to get dimensions
                            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeByteArray(sigBytes, 0, sigBytes.size, opts)
                            val imgWidthEmu = 2000000L // ~5.3cm
                            val imgHeightEmu = if (opts.outWidth > 0) imgWidthEmu * opts.outHeight / opts.outWidth else 800000L
                            bodyXml.append("""<w:p><w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"><wp:extent cx="$imgWidthEmu" cy="$imgHeightEmu"/><wp:docPr id="${docxImages.size}" name="Signature"/><a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:nvPicPr><pic:cNvPr id="0" name="$imgName"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="$rId" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$imgWidthEmu" cy="$imgHeightEmu"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>""")
                        }

                        addParagraph("")
                        addParagraph("o.g. Patient/Patientin wurde in meinem Beisein aufgeklärt:")
                        addParagraph("Name Zeuge/Angehöriger: ${docxRefusal.nameZeugeAngehoeriger}")
                        addParagraph("Adresse: ${docxRefusal.adresseZeugeAngehoeriger}")
                        addParagraph("")
                        addParagraph("o.g. Patient/Patientin wurde in meinem Beisein aufgeklärt:")
                        addParagraph("Name Zeuge/Rettungsdienst: ${docxRefusal.nameZeugeRettungsdienst}")
                        addParagraph("")
                        addParagraph("Die Aufklärung des o.g. Patienten/Patientin erfolgte durch:")
                        addParagraph("Name Rettungsdienst/Notarztdienst: ${docxRefusal.nameRettungsdienstNotarzt}")
                        addParagraph("")
                        addParagraph("Ort: ${docxRefusal.ort}    Datum: $docxDatumStr    Uhrzeit: $docxUhrzeitStr")
                    }
                }

                // Build DOCX (ZIP with XML)
                val exportDir = File(context.getExternalFilesDir(null), "exports")
                exportDir.mkdirs()
                val fileName = "Einsatz_${mission.einsatzNummer.ifBlank { mission.id.toString() }}_${System.currentTimeMillis()}.docx"
                val file = File(exportDir, fileName)

                writeDocxFile(file, bodyXml.toString(), docxImages)

                markAsExported()

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccess = true,
                        exportedFilePath = file,
                        exportedMimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, errorMessage = "DOCX-Export fehlgeschlagen: ${e.message}")
                }
            }
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun severityColorInt(severity: InjurySeverity): Int = when (severity) {
        InjurySeverity.LEICHT -> 0x8066BB6A.toInt()
        InjurySeverity.MITTEL -> 0x80FFA726.toInt()
        InjurySeverity.SCHWER -> 0x80EF5350.toInt()
    }

    private fun renderBodySilhouetteBitmap(regionSeverities: Map<BodyRegion, InjurySeverity>): Bitmap {
        val bmpW = 250
        val bmpH = 450
        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val w = bmpW.toFloat()
        val h = bmpH.toFloat()
        val outlineColor = 0xFF9E9E9E.toInt()

        val outlinePaint = Paint().apply { color = outlineColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }
        val fillPaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }
        val textPaint = Paint().apply { textSize = 9f; color = android.graphics.Color.DKGRAY; isAntiAlias = true; textAlign = Paint.Align.CENTER }

        fun drawRegion(region: BodyRegion, left: Float, top: Float, rw: Float, rh: Float) {
            regionSeverities[region]?.let {
                fillPaint.color = severityColorInt(it)
                canvas.drawRect(left, top, left + rw, top + rh, fillPaint)
            }
            canvas.drawRect(left, top, left + rw, top + rh, outlinePaint)
        }

        // Head
        val headCx = w * 0.5f; val headCy = h * 0.06f; val headR = w * 0.09f
        regionSeverities[BodyRegion.KOPF]?.let {
            fillPaint.color = severityColorInt(it)
            canvas.drawCircle(headCx, headCy, headR, fillPaint)
        }
        canvas.drawCircle(headCx, headCy, headR, outlinePaint)

        // Face
        regionSeverities[BodyRegion.GESICHT]?.let {
            fillPaint.color = severityColorInt(it)
            canvas.drawOval(android.graphics.RectF(headCx - headR * 0.5f, headCy - headR * 0.4f, headCx + headR * 0.5f, headCy + headR * 0.4f), fillPaint)
        }

        // Neck
        val neckTop = headCy + headR; val neckH = h * 0.03f
        drawRegion(BodyRegion.HALS, w * 0.44f, neckTop, w * 0.12f, neckH)

        // Torso
        val torsoTop = neckTop + neckH; val torsoW = w * 0.36f; val torsoLeft = w * 0.5f - torsoW * 0.5f
        val brustH = h * 0.12f
        drawRegion(BodyRegion.BRUST, torsoLeft, torsoTop, torsoW, brustH)
        val bauchTop = torsoTop + brustH; val bauchH = h * 0.10f
        drawRegion(BodyRegion.BAUCH, torsoLeft, bauchTop, torsoW, bauchH)
        val beckenTop = bauchTop + bauchH; val beckenH = h * 0.06f
        drawRegion(BodyRegion.BECKEN, torsoLeft, beckenTop, torsoW, beckenH)

        // Spine
        val wsX = w * 0.5f
        val spinePaint = Paint().apply { strokeWidth = 6f; isAntiAlias = true }
        regionSeverities[BodyRegion.WIRBELSAEULE]?.let {
            spinePaint.color = severityColorInt(it)
            canvas.drawLine(wsX, torsoTop, wsX, beckenTop, spinePaint)
        } ?: run {
            spinePaint.color = (outlineColor and 0x4DFFFFFF.toInt())
            canvas.drawLine(wsX, torsoTop, wsX, beckenTop, spinePaint)
        }

        // Arms
        val armW = w * 0.08f; val upperArmH = h * 0.12f; val forearmH = h * 0.11f; val handH = h * 0.05f
        val armRX = torsoLeft - armW - w * 0.02f
        drawRegion(BodyRegion.OBERARM_RECHTS, armRX, torsoTop, armW, upperArmH)
        drawRegion(BodyRegion.UNTERARM_RECHTS, armRX, torsoTop + upperArmH, armW, forearmH)
        drawRegion(BodyRegion.HAND_RECHTS, armRX, torsoTop + upperArmH + forearmH, armW, handH)
        val armLX = torsoLeft + torsoW + w * 0.02f
        drawRegion(BodyRegion.OBERARM_LINKS, armLX, torsoTop, armW, upperArmH)
        drawRegion(BodyRegion.UNTERARM_LINKS, armLX, torsoTop + upperArmH, armW, forearmH)
        drawRegion(BodyRegion.HAND_LINKS, armLX, torsoTop + upperArmH + forearmH, armW, handH)

        // Back
        val rueckenW = w * 0.07f; val rueckenLeft = armLX + armW + w * 0.02f
        drawRegion(BodyRegion.RUECKEN, rueckenLeft, torsoTop, rueckenW, brustH + bauchH)

        // Legs
        val legW = w * 0.12f; val legGap = w * 0.04f; val legTop = beckenTop + beckenH
        val thighH = h * 0.14f; val shinH = h * 0.14f; val footH = h * 0.04f
        val legRX = w * 0.5f - legGap * 0.5f - legW
        drawRegion(BodyRegion.OBERSCHENKEL_RECHTS, legRX, legTop, legW, thighH)
        drawRegion(BodyRegion.UNTERSCHENKEL_RECHTS, legRX, legTop + thighH, legW, shinH)
        drawRegion(BodyRegion.FUSS_RECHTS, legRX, legTop + thighH + shinH, legW, footH)
        val legLX = w * 0.5f + legGap * 0.5f
        drawRegion(BodyRegion.OBERSCHENKEL_LINKS, legLX, legTop, legW, thighH)
        drawRegion(BodyRegion.UNTERSCHENKEL_LINKS, legLX, legTop + thighH, legW, shinH)
        drawRegion(BodyRegion.FUSS_LINKS, legLX, legTop + thighH + shinH, legW, footH)

        // Labels
        canvas.drawText("R", armRX + armW / 2, torsoTop - 4f, textPaint)
        canvas.drawText("L", armLX + armW / 2, torsoTop - 4f, textPaint)

        // Legend at bottom
        val legendY = legTop + thighH + shinH + footH + 20f
        val legendPaint = Paint().apply { textSize = 10f; color = android.graphics.Color.BLACK; isAntiAlias = true }
        val severities = listOf(
            Triple("Leicht", 0x8066BB6A.toInt(), 0f),
            Triple("Mittel", 0x80FFA726.toInt(), 80f),
            Triple("Schwer", 0x80EF5350.toInt(), 160f)
        )
        for ((label, color, offset) in severities) {
            val lx = 25f + offset
            fillPaint.color = color
            canvas.drawRect(lx, legendY, lx + 14f, legendY + 14f, fillPaint)
            canvas.drawRect(lx, legendY, lx + 14f, legendY + 14f, outlinePaint)
            canvas.drawText(label, lx + 7f, legendY + 28f, textPaint)
        }

        return bitmap
    }

    private fun writeDocxFile(file: File, bodyContent: String, images: List<Pair<String, ByteArray>> = emptyList()) {
        ZipOutputStream(FileOutputStream(file)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            val contentTypes = StringBuilder()
            contentTypes.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>""")
            if (images.isNotEmpty()) {
                contentTypes.append("""
  <Default Extension="png" ContentType="image/png"/>""")
            }
            contentTypes.append("""
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>""")
            zos.write(contentTypes.toString().toByteArray())
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""".toByteArray())
            zos.closeEntry()

            // word/_rels/document.xml.rels
            zos.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            val rels = StringBuilder()
            rels.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            for ((key, _) in images) {
                val parts = key.split("|", limit = 2)
                val rId = parts[0]
                val imgName = parts[1]
                rels.append("""
  <Relationship Id="$rId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/$imgName"/>""")
            }
            rels.append("""
</Relationships>""")
            zos.write(rels.toString().toByteArray())
            zos.closeEntry()

            // word/media/ images
            for ((key, bytes) in images) {
                val imgName = key.split("|", limit = 2)[1]
                zos.putNextEntry(ZipEntry("word/media/$imgName"))
                zos.write(bytes)
                zos.closeEntry()
            }

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
            xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
    $bodyContent
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/>
    </w:sectPr>
  </w:body>
</w:document>""".toByteArray())
            zos.closeEntry()
        }
    }
}
