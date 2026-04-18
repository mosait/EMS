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
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.SignaturePad
import com.mosait.ems.core.ui.components.UnsavedChangesDialog
import com.mosait.ems.core.ui.util.DateTimeUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransportRefusalScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TransportRefusalViewModel = hiltViewModel()
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
                title = "Transportverweigerung",
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
            // Enable/Disable Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transportverweigerung aktivieren",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (uiState.enabled) "Wird im PDF/DOCX exportiert" else "Nicht im Export enthalten",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = { viewModel.toggleEnabled() }
                )
            }

            if (uiState.enabled) {
                // Patientendaten
            SectionHeader(title = "Patientendaten")
            EmsTextField(
                value = uiState.patientName,
                onValueChange = { viewModel.updatePatientName(it) },
                label = "Herr/Frau (Name)"
            )
            EmsTextField(
                value = uiState.geburtsdatum,
                onValueChange = { viewModel.updateGeburtsdatum(it) },
                label = "Geb. am"
            )
            EmsTextField(
                value = uiState.geburtsort,
                onValueChange = { viewModel.updateGeburtsort(it) },
                label = "Geb. in"
            )

            // Datum/Uhrzeit
            SectionHeader(title = "Datum / Uhrzeit der Aufklärung")
            EmsTextField(
                value = DateTimeUtil.formatDate(uiState.datum),
                onValueChange = { },
                label = "Datum",
                readOnly = true
            )
            EmsTextField(
                value = uiState.uhrzeit?.toString() ?: "",
                onValueChange = { },
                label = "Uhrzeit",
                readOnly = true
            )

            // Verweigerung
            SectionHeader(title = "Verweigerung von")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = uiState.lehntBehandlungAb,
                    onClick = { viewModel.toggleLehntBehandlungAb() },
                    label = { Text("eine Behandlung") }
                )
                FilterChip(
                    selected = uiState.lehntTransportAb,
                    onClick = { viewModel.toggleLehntTransportAb() },
                    label = { Text("Beförderung in ein Krankenhaus") }
                )
            }

            // Nicht auszuschließende Erkrankungen
            SectionHeader(title = "Nicht auszuschließende Erkrankungen/Verletzungen")
            Text(
                text = "Ohne klinische Abklärung sind folgende Verletzungen bzw. Erkrankungen nicht auszuschließen:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EmsTextField(
                value = uiState.nichtAuszuschliessendeErkrankungen,
                onValueChange = { viewModel.updateNichtAuszuschliessendeErkrankungen(it) },
                label = "Erkrankungen/Verletzungen",
                singleLine = false,
                maxLines = 4
            )

            // Mögliche Folgen
            SectionHeader(title = "Mögliche Folgen der Transportverweigerung")
            EmsTextField(
                value = uiState.moeglicheFolgen,
                onValueChange = { viewModel.updateMoeglicheFolgen(it) },
                label = "Mögliche Folgen",
                singleLine = false,
                maxLines = 4
            )

            // Zeugen / Unterschriften
            SectionHeader(title = "Zeugen")
            EmsTextField(
                value = uiState.nameZeugeAngehoeriger,
                onValueChange = { viewModel.updateNameZeugeAngehoeriger(it) },
                label = "Name Zeuge/Angehöriger"
            )
            EmsTextField(
                value = uiState.adresseZeugeAngehoeriger,
                onValueChange = { viewModel.updateAdresseZeugeAngehoeriger(it) },
                label = "Adresse Zeuge/Angehöriger"
            )
            EmsTextField(
                value = uiState.nameZeugeRettungsdienst,
                onValueChange = { viewModel.updateNameZeugeRettungsdienst(it) },
                label = "Name Zeuge/Rettungsdienst"
            )
            EmsTextField(
                value = uiState.nameRettungsdienstNotarzt,
                onValueChange = { viewModel.updateNameRettungsdienstNotarzt(it) },
                label = "Name Rettungsdienst/Notarztdienst (Aufklärung durch)"
            )

            // Ort
            SectionHeader(title = "Ort")
            EmsTextField(
                value = uiState.ort,
                onValueChange = { viewModel.updateOrt(it) },
                label = "Ort/Place"
            )

            // Unterschrift Patient
            SectionHeader(title = "Unterschrift Patient/Patientin")
            SignaturePad(
                signatureBytes = uiState.signaturePatient,
                onSignatureChanged = { viewModel.updateSignaturePatient(it) },
                modifier = Modifier.fillMaxWidth()
            )
            } // end if (uiState.enabled)

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
