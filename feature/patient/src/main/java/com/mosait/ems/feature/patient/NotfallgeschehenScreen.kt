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
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@Composable
fun NotfallgeschehenScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: NotfallgeschehenViewModel = hiltViewModel()
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
                title = "Notfallgeschehen",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Beschreibung des Notfallgeschehens",
                style = MaterialTheme.typography.titleMedium
            )

            EmsTextField(
                value = uiState.notfallgeschehen,
                onValueChange = { viewModel.updateNotfallgeschehen(it) },
                label = "Notfallgeschehen / Unfallhergang",
                singleLine = false,
                maxLines = 20
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
