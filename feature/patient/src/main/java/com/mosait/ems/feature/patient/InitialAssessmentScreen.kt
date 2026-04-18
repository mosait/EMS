package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.AtmungStatus
import com.mosait.ems.core.model.Bewusstseinslage
import com.mosait.ems.core.model.EkgRhythmus
import com.mosait.ems.core.model.PupillenStatus
import com.mosait.ems.core.ui.components.EmsChipGroup
import com.mosait.ems.core.ui.components.EmsNumberField
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.UnsavedChangesDialog
import com.mosait.ems.core.ui.util.DateTimeUtil

@Composable
fun InitialAssessmentScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InitialAssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (viewModel.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = "Erstbefund",
                onNavigateBack = handleBack,
                actions = {
                    TextButton(onClick = { viewModel.save(); onNavigateBack() }) {
                        Text("Speichern")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bewusstseinslage
            SectionHeader(title = "Bewusstseinslage")
            EmsChipGroup(
                items = Bewusstseinslage.entries,
                selectedItems = listOf(uiState.bewusstseinslage),
                onSelectionChanged = { viewModel.updateBewusstseinslage(it.firstOrNull() ?: Bewusstseinslage.ORIENTIERT) },
                labelSelector = { it.name },
                singleSelection = true
            )
            AnimatedVisibility(visible = uiState.bewusstseinslage == Bewusstseinslage.SONSTIGES) {
                EmsTextField(
                    value = uiState.bewusstseinslageText,
                    onValueChange = { viewModel.updateBewusstseinslageText(it) },
                    label = "Bewusstseinslage (Sonstiges)",
                    singleLine = false,
                    maxLines = 3
                )
            }

            // Kreislauf
            SectionHeader(title = "Kreislauf")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.kreislaufSchock,
                    onClick = { viewModel.toggleKreislaufSchock() },
                    label = { Text("Schock") }
                )
                FilterChip(
                    selected = uiState.kreislaufStillstand,
                    onClick = { viewModel.toggleKreislaufStillstand() },
                    label = { Text("Stillstand") }
                )
                FilterChip(
                    selected = uiState.kreislaufReanimation,
                    onClick = { viewModel.toggleKreislaufReanimation() },
                    label = { Text("Reanimation") }
                )
            }
            var kreislaufSonstiges by remember { mutableStateOf(uiState.kreislaufSonstigesText.isNotBlank()) }
            LaunchedEffect(uiState.kreislaufSonstigesText) {
                if (uiState.kreislaufSonstigesText.isNotBlank()) kreislaufSonstiges = true
            }
            FilterChip(
                selected = kreislaufSonstiges,
                onClick = { kreislaufSonstiges = !kreislaufSonstiges },
                label = { Text("Sonstiges") }
            )
            AnimatedVisibility(visible = kreislaufSonstiges) {
                EmsTextField(
                    value = uiState.kreislaufSonstigesText,
                    onValueChange = { viewModel.updateKreislaufSonstigesText(it) },
                    label = "Kreislauf (Sonstiges)",
                    singleLine = false,
                    maxLines = 3
                )
            }

            // Messwerte
            SectionHeader(title = "Messwerte")
            uiState.messwertZeit?.let { zeit ->
                Text(
                    text = "Erfasst: ${DateTimeUtil.formatTime(zeit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmsNumberField(
                    value = uiState.rrSystolisch,
                    onValueChange = { viewModel.updateRrSystolisch(it) },
                    label = "RR syst.",
                    suffix = "mmHg",
                    modifier = Modifier.weight(1f)
                )
                EmsNumberField(
                    value = uiState.rrDiastolisch,
                    onValueChange = { viewModel.updateRrDiastolisch(it) },
                    label = "RR diast.",
                    suffix = "mmHg",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmsNumberField(
                    value = uiState.puls,
                    onValueChange = { viewModel.updatePuls(it) },
                    label = "Puls",
                    suffix = "/min",
                    modifier = Modifier.weight(1f)
                )
                EmsNumberField(
                    value = uiState.spO2,
                    onValueChange = { viewModel.updateSpO2(it) },
                    label = "SpO₂",
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                    maxValue = 100
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmsNumberField(
                    value = uiState.atemfrequenz,
                    onValueChange = { viewModel.updateAtemfrequenz(it) },
                    label = "AF",
                    suffix = "/min",
                    modifier = Modifier.weight(1f)
                )
                EmsNumberField(
                    value = uiState.blutzucker,
                    onValueChange = { viewModel.updateBlutzucker(it) },
                    label = "BZ",
                    suffix = "mg/dl",
                    modifier = Modifier.weight(1f)
                )
            }

            // GCS
            SectionHeader(title = "GCS (${uiState.gcsAugen + uiState.gcsVerbal + uiState.gcsMotorik})")
            Text("Augen (${uiState.gcsAugen})", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = uiState.gcsAugen.toFloat(),
                onValueChange = { viewModel.updateGcsAugen(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2
            )
            Text("Verbal (${uiState.gcsVerbal})", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = uiState.gcsVerbal.toFloat(),
                onValueChange = { viewModel.updateGcsVerbal(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3
            )
            Text("Motorik (${uiState.gcsMotorik})", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = uiState.gcsMotorik.toFloat(),
                onValueChange = { viewModel.updateGcsMotorik(it.toInt()) },
                valueRange = 1f..6f,
                steps = 4
            )

            // Pupillen
            SectionHeader(title = "Pupillen")
            Text("Links", style = MaterialTheme.typography.bodyMedium)
            EmsChipGroup(
                items = PupillenStatus.entries,
                selectedItems = listOf(uiState.pupilleLinks),
                onSelectionChanged = { viewModel.updatePupilleLinks(it.firstOrNull() ?: PupillenStatus.MITTEL) },
                labelSelector = { it.name },
                singleSelection = true
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lichtreaktion:", style = MaterialTheme.typography.bodyMedium)
                FilterChip(
                    selected = uiState.lichtreaktionLinks,
                    onClick = { viewModel.updateLichtreaktionLinks(true) },
                    label = { Text("Ja") }
                )
                FilterChip(
                    selected = !uiState.lichtreaktionLinks,
                    onClick = { viewModel.updateLichtreaktionLinks(false) },
                    label = { Text("Nein") }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rechts", style = MaterialTheme.typography.bodyMedium)
            EmsChipGroup(
                items = PupillenStatus.entries,
                selectedItems = listOf(uiState.pupilleRechts),
                onSelectionChanged = { viewModel.updatePupilleRechts(it.firstOrNull() ?: PupillenStatus.MITTEL) },
                labelSelector = { it.name },
                singleSelection = true
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lichtreaktion:", style = MaterialTheme.typography.bodyMedium)
                FilterChip(
                    selected = uiState.lichtreaktionRechts,
                    onClick = { viewModel.updateLichtreaktionRechts(true) },
                    label = { Text("Ja") }
                )
                FilterChip(
                    selected = !uiState.lichtreaktionRechts,
                    onClick = { viewModel.updateLichtreaktionRechts(false) },
                    label = { Text("Nein") }
                )
            }

            // EKG
            SectionHeader(title = "EKG")
            EmsChipGroup(
                items = EkgRhythmus.entries,
                selectedItems = listOf(uiState.ekg),
                onSelectionChanged = { viewModel.updateEkg(it.firstOrNull() ?: EkgRhythmus.SINUS) },
                labelSelector = { it.name.replace("_", " ") },
                singleSelection = true
            )
            AnimatedVisibility(visible = uiState.ekg == EkgRhythmus.SONSTIGES) {
                EmsTextField(
                    value = uiState.ekgSonstigesText,
                    onValueChange = { viewModel.updateEkgSonstigesText(it) },
                    label = "EKG (Sonstiges)",
                    singleLine = false,
                    maxLines = 3
                )
            }

            // Schmerz
            SectionHeader(title = "Schmerz (NRS: ${uiState.schmerzSkala})")
            Slider(
                value = uiState.schmerzSkala.toFloat(),
                onValueChange = { viewModel.updateSchmerzSkala(it.toInt()) },
                valueRange = 0f..10f,
                steps = 9
            )

            // Atmung
            SectionHeader(title = "Atmung")
            EmsChipGroup(
                items = AtmungStatus.entries,
                selectedItems = listOf(uiState.atmung),
                onSelectionChanged = { viewModel.updateAtmung(it.firstOrNull() ?: AtmungStatus.SPONTAN) },
                labelSelector = { it.name },
                singleSelection = true
            )
            AnimatedVisibility(visible = uiState.atmung == AtmungStatus.SONSTIGES) {
                EmsTextField(
                    value = uiState.atmungSonstigesText,
                    onValueChange = { viewModel.updateAtmungSonstigesText(it) },
                    label = "Atmung (Sonstiges)",
                    singleLine = false,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = { viewModel.save(); onNavigateBack() },
            onDiscard = { onNavigateBack() }
        )
    }
}
