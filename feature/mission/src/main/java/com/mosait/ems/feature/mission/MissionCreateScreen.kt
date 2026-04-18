package com.mosait.ems.feature.mission

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.EinsatzArt
import com.mosait.ems.core.model.PersonalEntry
import com.mosait.ems.core.model.PersonalRolle
import com.mosait.ems.core.model.RettungsMittel
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.EmsChipGroup
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionCreateScreen(
    onNavigateBack: () -> Unit,
    onMissionCreated: (Long) -> Unit,
    viewModel: MissionFormViewModel = hiltViewModel()
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

    LaunchedEffect(uiState.savedMissionId) {
        uiState.savedMissionId?.let { id ->
            if (uiState.isEditMode) {
                onNavigateBack()
            } else {
                onMissionCreated(id)
            }
        }
    }

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = if (uiState.isEditMode) "Einsatz bearbeiten" else "Neuer Einsatz",
                onNavigateBack = handleBack
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Einsatzart")
            EmsChipGroup(
                items = EinsatzArt.entries,
                selectedItems = listOf(uiState.einsatzArt),
                onSelectionChanged = { viewModel.updateEinsatzArt(it.firstOrNull() ?: EinsatzArt.NOTFALLEINSATZ) },
                labelSelector = { it.name.replace("_", " ") },
                singleSelection = true
            )

            SectionHeader(title = "Rettungsmittel")
            EmsChipGroup(
                items = RettungsMittel.entries,
                selectedItems = listOf(uiState.rettungsMittel),
                onSelectionChanged = { viewModel.updateRettungsMittel(it.firstOrNull() ?: RettungsMittel.RTW) },
                labelSelector = { it.name },
                singleSelection = true
            )

            SectionHeader(title = "Einsatzdaten")
            EmsTextField(
                value = uiState.einsatzNummer,
                onValueChange = { viewModel.updateEinsatzNummer(it) },
                label = "Einsatznummer"
            )
            EmsTextField(
                value = uiState.funkKennung,
                onValueChange = { viewModel.updateFunkKennung(it) },
                label = "Funkkennung / Fahrzeugkennung"
            )

            // Besatzung
            SectionHeader(title = "Besatzung")

            uiState.personal.forEachIndexed { index, entry ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name.ifBlank { "Ohne Name" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = entry.rolle.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.removePersonal(index) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Entfernen",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Add new crew member
            var newPersonName by remember { mutableStateOf("") }
            var newPersonRolle by remember { mutableStateOf(PersonalRolle.RETTUNGSSANITAETER) }
            var rolleExpanded by remember { mutableStateOf(false) }

            EmsTextField(
                value = newPersonName,
                onValueChange = { newPersonName = it },
                label = "Name"
            )

            ExposedDropdownMenuBox(
                expanded = rolleExpanded,
                onExpandedChange = { rolleExpanded = it }
            ) {
                OutlinedTextField(
                    value = newPersonRolle.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rolle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = rolleExpanded,
                    onDismissRequest = { rolleExpanded = false }
                ) {
                    PersonalRolle.entries.forEach { rolle ->
                        DropdownMenuItem(
                            text = { Text(rolle.displayName) },
                            onClick = {
                                newPersonRolle = rolle
                                rolleExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    if (newPersonName.isNotBlank()) {
                        viewModel.addPersonal(PersonalEntry(name = newPersonName, rolle = newPersonRolle))
                        newPersonName = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = newPersonName.isNotBlank()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Besatzungsmitglied hinzufügen")
            }

            SectionHeader(title = "Einsatzort")
            EmsTextField(
                value = uiState.einsatzOrtStrasse,
                onValueChange = { viewModel.updateEinsatzOrtStrasse(it) },
                label = "Straße / Hausnummer"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmsTextField(
                    value = uiState.einsatzOrtPlz,
                    onValueChange = { viewModel.updateEinsatzOrtPlz(it) },
                    label = "PLZ",
                    modifier = Modifier.weight(1f)
                )
                EmsTextField(
                    value = uiState.einsatzOrtOrt,
                    onValueChange = { viewModel.updateEinsatzOrtOrt(it) },
                    label = "Ort",
                    modifier = Modifier.weight(2f)
                )
            }

            SectionHeader(title = "Transportziel")
            EmsTextField(
                value = uiState.transportZiel,
                onValueChange = { viewModel.updateTransportZiel(it) },
                label = "Krankenhaus / Ziel"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveMission() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isEditMode) "Änderungen speichern" else "Einsatz erstellen")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = { viewModel.saveMission(); showUnsavedDialog = false },
            onDiscard = { onNavigateBack() }
        )
    }
}
