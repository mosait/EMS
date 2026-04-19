package com.mosait.ems.feature.injury

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
import com.mosait.ems.core.model.BodyRegion
import com.mosait.ems.core.model.InjurySeverity
import com.mosait.ems.core.model.InjuryType
import com.mosait.ems.core.ui.components.EmsChipGroup
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader

@Composable
fun InjuryScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InjuryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = "Verletzung",
                onNavigateBack = onNavigateBack,
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
            FilterChip(
                selected = uiState.keine,
                onClick = { viewModel.toggleKeine() },
                label = { Text("Keine Verletzung") }
            )

            SectionHeader(title = "Verletzungsart")
            EmsChipGroup(
                items = InjuryType.entries,
                selectedItems = uiState.selectedTypes,
                onSelectionChanged = { viewModel.updateSelectedTypes(it) },
                labelSelector = { it.name.replace("_", " ") }
            )

            val regionGroups = mapOf(
                "Kopf / Hals" to listOf(BodyRegion.KOPF, BodyRegion.GESICHT, BodyRegion.HALS),
                "Oberkörper" to listOf(BodyRegion.BRUST, BodyRegion.BAUCH, BodyRegion.BECKEN, BodyRegion.RUECKEN, BodyRegion.WIRBELSAEULE),
                "Arme" to listOf(BodyRegion.OBERARM_LINKS, BodyRegion.OBERARM_RECHTS, BodyRegion.UNTERARM_LINKS, BodyRegion.UNTERARM_RECHTS, BodyRegion.HAND_LINKS, BodyRegion.HAND_RECHTS),
                "Beine" to listOf(BodyRegion.OBERSCHENKEL_LINKS, BodyRegion.OBERSCHENKEL_RECHTS, BodyRegion.UNTERSCHENKEL_LINKS, BodyRegion.UNTERSCHENKEL_RECHTS, BodyRegion.FUSS_LINKS, BodyRegion.FUSS_RECHTS)
            )

            regionGroups.forEach { (groupName, regions) ->
                SectionHeader(title = groupName)
                EmsChipGroup(
                    items = regions,
                    selectedItems = uiState.selectedRegions,
                    onSelectionChanged = { viewModel.updateSelectedRegions(it) },
                    labelSelector = { it.name.replace("_", " ") }
                )
                // Inline severity selection for selected regions in this group
                val selectedInGroup = regions.filter { it in uiState.selectedRegions }
                if (selectedInGroup.isNotEmpty()) {
                    selectedInGroup.forEach { region ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = region.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                InjurySeverity.entries.forEach { severity ->
                                    FilterChip(
                                        selected = uiState.regionSeverities[region] == severity,
                                        onClick = { viewModel.updateSeverityForRegion(region, severity) },
                                        label = { Text(severity.name, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SectionHeader(title = "Freitext")
            EmsTextField(
                value = uiState.freitext,
                onValueChange = { viewModel.updateFreitext(it) },
                label = "Weitere Angaben",
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
