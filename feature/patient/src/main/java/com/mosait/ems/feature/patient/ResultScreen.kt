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
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@Composable
fun ResultScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
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
                title = "Ergebnis / Übergabe",
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
            SectionHeader(title = "Zustand bei Übergabe")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.zustandVerbessert,
                    onClick = { viewModel.toggleZustandVerbessert() },
                    label = { Text("Verbessert") }
                )
                FilterChip(
                    selected = uiState.zustandUnveraendert,
                    onClick = { viewModel.toggleZustandUnveraendert() },
                    label = { Text("Unverändert") }
                )
                FilterChip(
                    selected = uiState.zustandVerschlechtert,
                    onClick = { viewModel.toggleZustandVerschlechtert() },
                    label = { Text("Verschlechtert") }
                )
            }

            SectionHeader(title = "Transport")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.transportNichtErforderlich,
                    onClick = { viewModel.toggleTransportNichtErforderlich() },
                    label = { Text("Nicht erforderlich") }
                )
                FilterChip(
                    selected = uiState.patientLehntTransportAb,
                    onClick = { viewModel.togglePatientLehntTransportAb() },
                    label = { Text("Patient lehnt ab") }
                )
            }

            SectionHeader(title = "Notarzt")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.notarztNachgefordert,
                    onClick = { viewModel.toggleNotarztNachgefordert() },
                    label = { Text("Nachgefordert") }
                )
                FilterChip(
                    selected = uiState.notarztAbbestellt,
                    onClick = { viewModel.toggleNotarztAbbestellt() },
                    label = { Text("Abbestellt") }
                )
            }

            SectionHeader(title = "Sonstiges")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.hausarztInformiert,
                    onClick = { viewModel.toggleHausarztInformiert() },
                    label = { Text("Hausarzt informiert") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.todAmNotfallort,
                    onClick = { viewModel.toggleTodAmNotfallort() },
                    label = { Text("Tod am Notfallort") }
                )
                FilterChip(
                    selected = uiState.todWaehrendTransport,
                    onClick = { viewModel.toggleTodWaehrendTransport() },
                    label = { Text("Tod während Transport") }
                )
            }

            EmsTextField(
                value = uiState.sonstigesFreitext,
                onValueChange = { viewModel.updateSonstigesFreitext(it) },
                label = "Sonstiges (Freitext)",
                singleLine = false,
                maxLines = 4
            )

            SectionHeader(title = "NACA-Score (${uiState.nacaScore ?: "-"})")
            Slider(
                value = (uiState.nacaScore ?: 0).toFloat(),
                onValueChange = { viewModel.updateNacaScore(it.toInt()) },
                valueRange = 0f..7f,
                steps = 6
            )
            Text(
                text = when (uiState.nacaScore) {
                    0 -> "0 – Keine Verletzung/Erkrankung"
                    1 -> "I – Geringfügig"
                    2 -> "II – Leicht"
                    3 -> "III – Mäßig bis schwer"
                    4 -> "IV – Schwer, akute Lebensgefahr nicht auszuschließen"
                    5 -> "V – Akute Lebensgefahr"
                    6 -> "VI – Reanimation"
                    7 -> "VII – Tod"
                    else -> "Bitte auswählen"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionHeader(title = "Übergabe")
            EmsTextField(
                value = uiState.uebergabeAn,
                onValueChange = { viewModel.updateUebergabeAn(it) },
                label = "Übergabe an (Name / Station)"
            )

            SectionHeader(title = "Wertsachen")
            EmsTextField(
                value = uiState.wertsachen,
                onValueChange = { viewModel.updateWertsachen(it) },
                label = "Wertsachen / Persönliche Gegenstände",
                singleLine = false,
                maxLines = 3
            )

            SectionHeader(title = "Verlaufsbeschreibung")
            EmsTextField(
                value = uiState.verlaufsbeschreibung,
                onValueChange = { viewModel.updateVerlaufsbeschreibung(it) },
                label = "Einsatz- und Behandlungsverlauf",
                singleLine = false,
                maxLines = 8
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
