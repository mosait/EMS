package com.mosait.ems.feature.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.EinsatzArt
import com.mosait.ems.core.model.Patient
import com.mosait.ems.core.model.RettungsMittel
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MissionDetailScreen(
    missionId: Long,
    onNavigateBack: () -> Unit,
    onAddPatient: (Long) -> Unit,
    onPatientClick: (Long, Long) -> Unit,
    onExport: (Long) -> Unit,
    onEditMission: (Long) -> Unit,
    onEditPatient: (Long, Long) -> Unit,
    viewModel: MissionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()

    var showDeleteMissionDialog by remember { mutableStateOf(false) }
    var patientToDelete by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) onNavigateBack()
    }

    if (showDeleteMissionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteMissionDialog = false },
            title = { Text("Einsatz löschen") },
            text = { Text("Soll dieser Einsatz mit allen zugehörigen Patienten und Protokolldaten unwiderruflich gelöscht werden?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteMissionDialog = false
                        viewModel.deleteMission()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMissionDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    patientToDelete?.let { patient ->
        AlertDialog(
            onDismissRequest = { patientToDelete = null },
            title = { Text("Patient löschen") },
            text = {
                Text(
                    "Soll der Patient \"${patient.nachname}, ${patient.vorname}\" mit allen Protokolldaten unwiderruflich gelöscht werden?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePatient(patient.id)
                        patientToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { patientToDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = uiState.mission?.let {
                    if (it.einsatzNummer.isNotBlank()) "Einsatz ${it.einsatzNummer}" else "Einsatz #${it.id}"
                } ?: "Einsatz",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { onEditMission(missionId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                    IconButton(onClick = { showDeleteMissionDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Löschen")
                    }
                    IconButton(onClick = { onExport(missionId) }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportieren")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddPatient(missionId) },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Patient hinzufügen") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.mission?.let { mission ->
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Einsatzinformationen", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Art: ${if (mission.einsatzArt == EinsatzArt.SONSTIGES && mission.einsatzArtSonstiges.isNotBlank()) mission.einsatzArtSonstiges else mission.einsatzArt.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("Mittel: ${if (mission.rettungsMittel == RettungsMittel.SONSTIGES && mission.rettungsMittelSonstiges.isNotBlank()) mission.rettungsMittelSonstiges else mission.rettungsMittel.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("Datum: ${DateTimeUtil.formatDate(mission.einsatzDatum)}", style = MaterialTheme.typography.bodyMedium)
                            if (mission.funkKennung.isNotBlank()) {
                                Text("Funkkennung / Fahrzeugkennung: ${mission.funkKennung}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (mission.personal.isNotEmpty()) {
                                Text("Besatzung: ${mission.personal.joinToString(", ") { "${it.name} (${it.rolle.displayName})" }}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (mission.einsatzOrtStrasse.isNotBlank()) {
                                Text("Ort: ${mission.einsatzOrtStrasse}, ${mission.einsatzOrtPlz} ${mission.einsatzOrtOrt}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (mission.transportZiel.isNotBlank()) {
                                val detailContext = LocalContext.current
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Ziel: ${mission.transportZiel}", style = MaterialTheme.typography.bodyMedium)
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("geo:0,0?q=${Uri.encode(mission.transportZiel)}")
                                            )
                                            detailContext.startActivity(intent)
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
                        }
                    }
                }

                item {
                    SectionHeader(title = "Patienten (${uiState.patients.size})")
                }

                if (uiState.patients.isEmpty()) {
                    item {
                        Text(
                            text = "Noch keine Patienten erfasst",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(uiState.patients, key = { it.id }) { patient ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onPatientClick(missionId, patient.id) },
                                onLongClick = { patientToDelete = patient }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (patient.nachname.isNotBlank() || patient.vorname.isNotBlank())
                                        "${patient.nachname}, ${patient.vorname}"
                                    else "Patient #${patient.id}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = DateTimeUtil.formatDate(patient.geburtsdatum),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEditPatient(missionId, patient.id) }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Patient bearbeiten",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
