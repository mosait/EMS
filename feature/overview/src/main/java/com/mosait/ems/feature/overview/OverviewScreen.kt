package com.mosait.ems.feature.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.ui.components.EmsTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onCreateMission: () -> Unit,
    onMissionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var missionToDelete by remember { mutableStateOf<Mission?>(null) }

    missionToDelete?.let { mission ->
        AlertDialog(
            onDismissRequest = { missionToDelete = null },
            title = { Text("Einsatz löschen") },
            text = {
                Text(
                    "Soll der Einsatz \"${
                        if (mission.einsatzNummer.isNotBlank()) mission.einsatzNummer else "#${mission.id}"
                    }\" mit allen zugehörigen Daten unwiderruflich gelöscht werden?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMission(mission.id)
                        missionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { missionToDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = "Einsätze",
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Einstellungen"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateMission,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Neuer Einsatz") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (uiState.missions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Keine Einsätze vorhanden",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Erstelle einen neuen Einsatz mit dem + Button",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.missions, key = { it.id }) { mission ->
                    MissionCard(
                        mission = mission,
                        onClick = { onMissionClick(mission.id) },
                        onLongClick = { missionToDelete = mission }
                    )
                }
            }
        }
    }
}
