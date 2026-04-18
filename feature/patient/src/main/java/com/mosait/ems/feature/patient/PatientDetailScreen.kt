package com.mosait.ems.feature.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.ui.components.EmsTopAppBar

@Composable
fun PatientDetailScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    onEditPatient: (Long) -> Unit,
    onNavigateToAssessment: (Long) -> Unit,
    onNavigateToNotfallgeschehen: (Long) -> Unit,
    onNavigateToDiagnosis: (Long) -> Unit,
    onNavigateToInjury: (Long) -> Unit,
    onNavigateToVitals: (Long) -> Unit,
    onNavigateToMeasures: (Long) -> Unit,
    onNavigateToResult: (Long) -> Unit,
    onNavigateToInfectionProtocol: (Long) -> Unit,
    onNavigateToTransportRefusal: (Long) -> Unit,
    viewModel: PatientDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) onNavigateBack()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Patient löschen") },
            text = {
                Text(
                    "Soll dieser Patient mit allen Protokolldaten unwiderruflich gelöscht werden?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePatient()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = uiState.patient?.let {
                    if (it.nachname.isNotBlank()) "${it.nachname}, ${it.vorname}" else "Patient #${it.id}"
                } ?: "Patient",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEditPatient(patientId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Löschen")
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
            uiState.patient?.let { patient ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stammdaten", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Name: ${patient.nachname}, ${patient.vorname}")
                        Text("Geb.: ${DateTimeUtil.formatDate(patient.geburtsdatum)}")
                        Text("Geschlecht: ${patient.geschlecht.name}")
                        if (patient.krankenkasse.isNotBlank()) {
                            Text("Kasse: ${patient.krankenkasse}")
                        }
                    }
                }
            }

            Text(
                text = "Protokollsektionen",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            ProtocolSectionButton(
                title = "Notfallgeschehen",
                subtitle = "Unfallhergang / Notfallbeschreibung",
                icon = Icons.Default.Description,
                onClick = { onNavigateToNotfallgeschehen(patientId) }
            )
            ProtocolSectionButton(
                title = "Erstbefund",
                subtitle = "Bewusstsein, Kreislauf, Messwerte, Pupillen, EKG",
                icon = Icons.Default.MonitorHeart,
                onClick = { onNavigateToAssessment(patientId) }
            )
            ProtocolSectionButton(
                title = "Erkrankung",
                subtitle = "Diagnose / Erkrankungsbild",
                icon = Icons.Default.MedicalServices,
                onClick = { onNavigateToDiagnosis(patientId) }
            )
            ProtocolSectionButton(
                title = "Infektionsprotokoll",
                subtitle = "Infektionen, Schutzmaßnahmen, Desinfektion",
                icon = Icons.Default.Shield,
                onClick = { onNavigateToInfectionProtocol(patientId) }
            )
            ProtocolSectionButton(
                title = "Verletzung",
                subtitle = "Verletzungsart und betroffene Körperregionen",
                icon = Icons.Default.PersonalInjury,
                onClick = { onNavigateToInjury(patientId) }
            )
            ProtocolSectionButton(
                title = "Vitalwerte-Verlauf",
                subtitle = "Messwerte im Zeitverlauf",
                icon = Icons.Default.Timeline,
                onClick = { onNavigateToVitals(patientId) }
            )
            ProtocolSectionButton(
                title = "Maßnahmen",
                subtitle = "Durchgeführte Maßnahmen",
                icon = Icons.Default.Healing,
                onClick = { onNavigateToMeasures(patientId) }
            )
            ProtocolSectionButton(
                title = "Ergebnis / Übergabe",
                subtitle = "Zustand, Transport, Übergabe",
                icon = Icons.Default.AssignmentTurnedIn,
                onClick = { onNavigateToResult(patientId) }
            )
            ProtocolSectionButton(
                title = "Transportverweigerung",
                subtitle = "Verweigerungserklärung des Patienten",
                icon = Icons.Default.Block,
                onClick = { onNavigateToTransportRefusal(patientId) }
            )
        }
    }
}

@Composable
private fun ProtocolSectionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
