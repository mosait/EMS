package com.mosait.ems.feature.mission

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                .imePadding()
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
            if (uiState.einsatzArt == EinsatzArt.SONSTIGES) {
                EmsTextField(
                    value = uiState.einsatzArtSonstiges,
                    onValueChange = { viewModel.updateEinsatzArtSonstiges(it) },
                    label = "Einsatzart (Freitext)"
                )
            }

            SectionHeader(title = "Rettungsmittel")
            EmsChipGroup(
                items = RettungsMittel.entries,
                selectedItems = listOf(uiState.rettungsMittel),
                onSelectionChanged = { viewModel.updateRettungsMittel(it.firstOrNull() ?: RettungsMittel.RTW) },
                labelSelector = { it.name },
                singleSelection = true
            )
            if (uiState.rettungsMittel == RettungsMittel.SONSTIGES) {
                EmsTextField(
                    value = uiState.rettungsMittelSonstiges,
                    onValueChange = { viewModel.updateRettungsMittelSonstiges(it) },
                    label = "Rettungsmittel (Freitext)"
                )
            }

            SectionHeader(title = "Einsatzdaten")
            EmsTextField(
                value = uiState.einsatzNummer,
                onValueChange = { viewModel.updateEinsatzNummer(it) },
                label = "Einsatznummer",
                isRequired = uiState.einsatzArt == EinsatzArt.NOTFALLEINSATZ || uiState.einsatzArt == EinsatzArt.KRANKENTRANSPORT,
                isError = uiState.einsatzNummerError,
                errorMessage = if (uiState.einsatzNummerError) "Pflichtfeld" else null
            )
            EmsTextField(
                value = uiState.funkKennung,
                onValueChange = { viewModel.updateFunkKennung(it) },
                label = "Funkkennung / Fahrzeugkennung",
                isRequired = uiState.einsatzArt == EinsatzArt.NOTFALLEINSATZ,
                isError = uiState.funkKennungError,
                errorMessage = if (uiState.funkKennungError) "Pflichtfeld" else null
            )

            // Besatzung
            SectionHeader(title = "Besatzung")

            if (uiState.personalError) {
                Text(
                    text = "Mindestens ein Besatzungsmitglied erforderlich",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

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
                label = "Straße / Hausnummer",
                isRequired = uiState.einsatzArt == EinsatzArt.NOTFALLEINSATZ,
                isError = uiState.einsatzOrtStrasseError,
                errorMessage = if (uiState.einsatzOrtStrasseError) "Pflichtfeld" else null
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
                    modifier = Modifier.weight(2f),
                    isRequired = uiState.einsatzArt == EinsatzArt.NOTFALLEINSATZ,
                    isError = uiState.einsatzOrtOrtError,
                    errorMessage = if (uiState.einsatzOrtOrtError) "Pflichtfeld" else null
                )
            }

            SectionHeader(title = "Transportziel")

            // Hospital autocomplete free text field
            val hospitalSuggestions = listOf(
                // Rems-Murr-Kreis
                "Rems-Murr-Klinikum Winnenden",
                "Rems-Murr-Klinik Schorndorf",
                "Klinik Erbach (Privatambulanz Weinstadt)",
                // Stuttgart
                "Klinikum Stuttgart – Katharinenhospital",
                "Klinikum Stuttgart – Bürgerhospital",
                "Klinikum Stuttgart – Olgahospital / Frauenklinik",
                "Robert-Bosch-Krankenhaus (Stuttgart)",
                "Robert-Bosch-Krankenhaus Standort 2 – City (Stuttgart)",
                "Diakonie-Klinikum Stuttgart",
                "Marienhospital Stuttgart",
                "Karl-Olga-Krankenhaus (Stuttgart)",
                "Krankenhaus Bad Cannstatt Stuttgart (KBC)",
                "Bethesda Krankenhaus Stuttgart",
                "Evangelisches Krankenhaus Bad Cannstatt Stuttgart",
                "Klinik Schillerhöhe (Robert Bosch - Gerlingen)",
                "St. Anna Klinik (Stuttgart)",
                "Sana Klinik Stuttgart",
                "Filderklinik (Filderstadt)",
                "Kreiskliniken Esslingen – Standort Esslingen",
                "Kreiskliniken Esslingen – Standort Nürtingen",
                "Kreiskliniken Esslingen – Standort Kirchheim",
                "Klinikum Ludwigsburg",
                "Klinik Bietigheim",
            )
            val filteredSuggestions = remember(uiState.transportZiel) {
                if (uiState.transportZiel.isBlank()) {
                    hospitalSuggestions
                } else {
                    hospitalSuggestions.filter {
                        it.contains(uiState.transportZiel, ignoreCase = true)
                    }
                }
            }
            var showSuggestions by remember { mutableStateOf(false) }
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            LaunchedEffect(showSuggestions, filteredSuggestions.size) {
                if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                    bringIntoViewRequester.bringIntoView()
                }
            }

            val navContext = LocalContext.current

            Column(
                modifier = Modifier
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.hasFocus) showSuggestions = true
                        else showSuggestions = false
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmsTextField(
                        value = uiState.transportZiel,
                        onValueChange = {
                            viewModel.updateTransportZiel(it)
                            showSuggestions = true
                        },
                        label = "Krankenhaus / Ziel",
                        modifier = Modifier.weight(1f),
                        isRequired = uiState.einsatzArt == EinsatzArt.KRANKENTRANSPORT,
                        isError = uiState.transportZielError,
                        errorMessage = if (uiState.transportZielError) "Pflichtfeld" else null
                    )
                    if (uiState.transportZiel.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=${Uri.encode(uiState.transportZiel)}")
                                )
                                navContext.startActivity(intent)
                            }
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = "Navigieren",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (showSuggestions && filteredSuggestions.isNotEmpty() &&
                    filteredSuggestions.none { it.equals(uiState.transportZiel, ignoreCase = true) }
                ) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val suggestionsScrollState = rememberScrollState()
                        val isScrolledToBottom = suggestionsScrollState.value >= suggestionsScrollState.maxValue
                        Box {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 192.dp)
                                    .verticalScroll(suggestionsScrollState)
                            ) {
                                filteredSuggestions.forEach { suggestion ->
                                    TextButton(
                                        onClick = {
                                            viewModel.updateTransportZiel(suggestion)
                                            showSuggestions = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = suggestion,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            if (filteredSuggestions.size > 4 && !isScrolledToBottom) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF212121),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

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
