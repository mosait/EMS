package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.MeasureCategories
import com.mosait.ems.core.ui.components.EmsStringChipGroup
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@Composable
fun MeasuresScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MeasuresViewModel = hiltViewModel()
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
                title = "Maßnahmen",
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
            SectionHeader(title = "Ersthelfer-Maßnahmen")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.ersthelferSuffizient,
                    onClick = { viewModel.toggleErsthelferSuffizient() },
                    label = { Text("Suffizient") }
                )
                FilterChip(
                    selected = uiState.ersthelferInsuffizient,
                    onClick = { viewModel.toggleErsthelferInsuffizient() },
                    label = { Text("Insuffizient") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.ersthelferAed,
                    onClick = { viewModel.toggleErsthelferAed() },
                    label = { Text("AED") }
                )
                FilterChip(
                    selected = uiState.ersthelferKeine,
                    onClick = { viewModel.toggleErsthelferKeine() },
                    label = { Text("Keine") }
                )
            }

            MeasureCategories.ALL_CATEGORIES.forEach { (category, measures) ->
                SectionHeader(title = category)
                EmsStringChipGroup(
                    items = measures,
                    selectedItems = uiState.selectedMeasures,
                    onSelectionChanged = { viewModel.updateSelectedMeasures(it) }
                )
                val sonstigesItem = measures.lastOrNull { it.startsWith("Sonstiges") }
                val isSonstigesSelected = sonstigesItem != null && uiState.selectedMeasures.contains(sonstigesItem)
                AnimatedVisibility(visible = isSonstigesSelected) {
                    EmsTextField(
                        value = uiState.sonstigesTexte[category] ?: "",
                        onValueChange = { viewModel.updateSonstigesText(category, it) },
                        label = "$category (Sonstiges)",
                        singleLine = false,
                        maxLines = 3
                    )
                }
            }

            SectionHeader(title = "Sauerstoff")
            EmsTextField(
                value = uiState.sauerstoffText,
                onValueChange = { viewModel.updateSauerstoff(it) },
                label = "Sauerstoff (l/min)"
            )

            SectionHeader(title = "Medikamente")
            EmsTextField(
                value = uiState.medikamente,
                onValueChange = { viewModel.updateMedikamente(it) },
                label = "Medikamente & Dosierung",
                singleLine = false,
                maxLines = 5
            )

            SectionHeader(title = "Sonstige Maßnahmen")
            EmsTextField(
                value = uiState.sonstige,
                onValueChange = { viewModel.updateSonstige(it) },
                label = "Weitere Maßnahmen",
                singleLine = false,
                maxLines = 4
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
