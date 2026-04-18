package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.InfectionOptions
import com.mosait.ems.core.ui.components.EmsStringChipGroup
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfectionProtocolScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InfectionProtocolViewModel = hiltViewModel()
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
                title = "Infektionsprotokoll",
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bekannte Infektionen
            SectionHeader(title = "Bekannte Infektionen des Patienten")
            EmsStringChipGroup(
                items = InfectionOptions.BEKANNTE_INFEKTIONEN,
                selectedItems = uiState.bekannteInfektionen,
                onSelectionChanged = { viewModel.updateBekannteInfektionen(it) }
            )
            EmsTextField(
                value = uiState.infektionFreitext,
                onValueChange = { viewModel.updateInfektionFreitext(it) },
                label = "Weitere Infektionen (Freitext)",
                singleLine = false,
                maxLines = 3
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Schutzmaßnahmen
            SectionHeader(title = "Getragene Schutzmaßnahmen")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = uiState.schutzHandschuhe,
                    onClick = { viewModel.toggleSchutzHandschuhe() },
                    label = { Text("Handschuhe") }
                )
                FilterChip(
                    selected = uiState.schutzMundschutz,
                    onClick = { viewModel.toggleSchutzMundschutz() },
                    label = { Text("Mundschutz") }
                )
                FilterChip(
                    selected = uiState.schutzFFP2,
                    onClick = { viewModel.toggleSchutzFFP2() },
                    label = { Text("FFP2") }
                )
                FilterChip(
                    selected = uiState.schutzSchutzbrille,
                    onClick = { viewModel.toggleSchutzSchutzbrille() },
                    label = { Text("Schutzbrille") }
                )
                FilterChip(
                    selected = uiState.schutzSchutzkittel,
                    onClick = { viewModel.toggleSchutzSchutzkittel() },
                    label = { Text("Schutzkittel") }
                )
            }
            EmsTextField(
                value = uiState.schutzSonstiges,
                onValueChange = { viewModel.updateSchutzSonstiges(it) },
                label = "Sonstige Schutzmaßnahmen"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Exposition
            SectionHeader(title = "Exposition / Kontamination")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = uiState.expositionKeine,
                    onClick = { viewModel.toggleExpositionKeine() },
                    label = { Text("Keine") }
                )
                FilterChip(
                    selected = uiState.expositionStichverletzung,
                    onClick = { viewModel.toggleExpositionStichverletzung() },
                    label = { Text("Stich-/Schnittverletzung") }
                )
                FilterChip(
                    selected = uiState.expositionSchleimhaut,
                    onClick = { viewModel.toggleExpositionSchleimhaut() },
                    label = { Text("Schleimhautkontakt") }
                )
                FilterChip(
                    selected = uiState.expositionHautkontakt,
                    onClick = { viewModel.toggleExpositionHautkontakt() },
                    label = { Text("Hautkontakt") }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Desinfektion
            SectionHeader(title = "Desinfektion nach Einsatz")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = uiState.fahrzeugDesinfiziert,
                    onClick = { viewModel.toggleFahrzeugDesinfiziert() },
                    label = { Text("Fahrzeug desinfiziert") }
                )
                FilterChip(
                    selected = uiState.geraeteDesinfiziert,
                    onClick = { viewModel.toggleGeraeteDesinfiziert() },
                    label = { Text("Geräte desinfiziert") }
                )
                FilterChip(
                    selected = uiState.waescheGewechselt,
                    onClick = { viewModel.toggleWaescheGewechselt() },
                    label = { Text("Wäsche gewechselt") }
                )
            }
            EmsTextField(
                value = uiState.desinfektionsmittel,
                onValueChange = { viewModel.updateDesinfektionsmittel(it) },
                label = "Verwendetes Desinfektionsmittel"
            )
            EmsTextField(
                value = uiState.desinfektionDurchgefuehrtVon,
                onValueChange = { viewModel.updateDesinfektionDurchgefuehrtVon(it) },
                label = "Desinfektion durchgeführt von"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bemerkungen
            SectionHeader(title = "Bemerkungen")
            EmsTextField(
                value = uiState.bemerkungen,
                onValueChange = { viewModel.updateBemerkungen(it) },
                label = "Bemerkungen",
                singleLine = false,
                maxLines = 5
            )

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
