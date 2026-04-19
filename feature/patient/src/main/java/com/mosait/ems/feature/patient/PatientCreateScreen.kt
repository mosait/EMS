package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.model.Geschlecht
import com.mosait.ems.core.ui.components.EmsChipGroup
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.mosait.ems.core.ui.components.SectionHeader
import com.mosait.ems.core.ui.components.UnsavedChangesDialog
import com.mosait.ems.core.ui.util.DateVisualTransformation

@Composable
fun PatientCreateScreen(
    missionId: Long,
    onNavigateBack: () -> Unit,
    onPatientCreated: (Long, Long) -> Unit,
    viewModel: PatientFormViewModel = hiltViewModel()
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

    LaunchedEffect(uiState.savedPatientId) {
        uiState.savedPatientId?.let { patientId ->
            if (uiState.isEditMode) {
                onNavigateBack()
            } else {
                onPatientCreated(missionId, patientId)
            }
        }
    }

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = if (uiState.isEditMode) "Patient bearbeiten" else "Neuer Patient",
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
            SectionHeader(title = "Personalien")
            EmsTextField(
                value = uiState.nachname,
                onValueChange = { viewModel.updateNachname(it) },
                label = "Nachname",
                isRequired = true,
                isError = uiState.nachnameError,
                errorMessage = if (uiState.nachnameError) "Pflichtfeld" else null
            )
            EmsTextField(
                value = uiState.vorname,
                onValueChange = { viewModel.updateVorname(it) },
                label = "Vorname",
                isRequired = true,
                isError = uiState.vornameError,
                errorMessage = if (uiState.vornameError) "Pflichtfeld" else null
            )
            EmsTextField(
                value = uiState.geburtsdatumText,
                onValueChange = { viewModel.updateGeburtsdatum(it) },
                label = "Geburtsdatum (TT.MM.JJJJ)",
                isRequired = true,
                isError = uiState.geburtsdatumError,
                errorMessage = uiState.geburtsdatumErrorMessage,
                keyboardType = KeyboardType.Number,
                visualTransformation = DateVisualTransformation()
            )
            EmsChipGroup(
                items = Geschlecht.entries,
                selectedItems = listOf(uiState.geschlecht),
                onSelectionChanged = { viewModel.updateGeschlecht(it.firstOrNull() ?: Geschlecht.UNBEKANNT) },
                labelSelector = { it.name },
                singleSelection = true
            )

            SectionHeader(title = "Versicherungsdaten")
            EmsTextField(
                value = uiState.krankenkasse,
                onValueChange = { viewModel.updateKrankenkasse(it) },
                label = "Krankenkasse",
                isRequired = true,
                isError = uiState.krankenkasseError,
                errorMessage = if (uiState.krankenkasseError) "Pflichtfeld" else null
            )
            EmsTextField(
                value = uiState.versichertenNummer,
                onValueChange = { viewModel.updateVersichertenNummer(it) },
                label = "Versicherten-Nr.",
                isRequired = true,
                isError = uiState.versichertenNummerError,
                errorMessage = if (uiState.versichertenNummerError) "Pflichtfeld" else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.savePatient(missionId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isEditMode) "Änderungen speichern" else "Patient anlegen")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        }
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = { viewModel.savePatient(missionId); showUnsavedDialog = false },
            onDiscard = { onNavigateBack() }
        )
    }
}
