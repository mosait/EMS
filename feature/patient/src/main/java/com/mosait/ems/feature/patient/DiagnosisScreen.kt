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
import com.mosait.ems.core.model.DiagnosisCategories
import com.mosait.ems.core.ui.components.EmsStringChipGroup
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@Composable
fun DiagnosisScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DiagnosisViewModel = hiltViewModel()
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
                title = "Erkrankung",
                onNavigateBack = handleBack,
                actions = {
                    TextButton(onClick = { if (viewModel.save()) onNavigateBack() }) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.keine,
                    onClick = { viewModel.toggleKeine() },
                    label = { Text("Keine Erkrankung") }
                )
            }

            if (uiState.selectionError) {
                Text(
                    text = "Bitte mindestens eine Erkrankung auswählen oder Freitext eingeben, oder \"Keine Erkrankung\" aktivieren",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            DiagnosisCategories.ALL_CATEGORIES.forEach { (category, conditions) ->
                SectionHeader(title = category)
                EmsStringChipGroup(
                    items = conditions,
                    selectedItems = uiState.selectedConditions,
                    onSelectionChanged = { viewModel.updateSelectedConditions(it) }
                )
                // Show inline text field when the category's Sonstiges is selected
                val sonstigesItem = conditions.lastOrNull { it.startsWith("Sonstiges") }
                val isSonstigesSelected = sonstigesItem != null && uiState.selectedConditions.contains(sonstigesItem)
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

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = { viewModel.save(); onNavigateBack() },
            onDiscard = { onNavigateBack() }
        )
    }
}
