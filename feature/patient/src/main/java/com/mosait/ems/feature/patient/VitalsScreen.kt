package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.model.VitalSign
import com.mosait.ems.core.ui.components.EmsNumberField
import com.mosait.ems.core.ui.components.EmsTextField
import com.mosait.ems.core.ui.components.EmsTopAppBar
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: VitalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingVitalSign by remember { mutableStateOf<VitalSign?>(null) }
    var showChart by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (showChart) {
            showChart = false
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = "Vitalwerte-Verlauf",
                onNavigateBack = handleBack,
                actions = {
                    if (uiState.vitalSigns.size >= 2) {
                        IconButton(onClick = { showChart = !showChart }) {
                            Icon(
                                if (showChart) Icons.Default.ViewList else Icons.Default.ShowChart,
                                contentDescription = if (showChart) "Liste" else "Diagramm"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingVitalSign = null
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Messung") }
            )
        }
    ) { padding ->
        if (uiState.vitalSigns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Keine Messwerte vorhanden",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Neue Messung mit dem + Button hinzufügen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (showChart && uiState.vitalSigns.size >= 2) {
            VitalsChart(
                vitalSigns = uiState.vitalSigns,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.vitalSigns, key = { it.id }) { vital ->
                    VitalSignCard(
                        vitalSign = vital,
                        onEdit = {
                            editingVitalSign = vital
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteVitalSign(vital.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showDialog) {
        VitalSignDialog(
            vitalSign = editingVitalSign,
            onDismiss = { showDialog = false },
            onSave = { vital ->
                if (editingVitalSign != null) {
                    viewModel.updateVitalSign(vital)
                } else {
                    viewModel.addVitalSign(vital)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun VitalSignCard(
    vitalSign: VitalSign,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtil.formatTime(vitalSign.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    vitalSign.rrSystolisch?.let { sys ->
                        val dias = vitalSign.rrDiastolisch?.let { "/$it" } ?: ""
                        Text("RR: $sys$dias mmHg", style = MaterialTheme.typography.bodyMedium)
                    }
                    vitalSign.puls?.let {
                        Text("Puls: $it /min", style = MaterialTheme.typography.bodyMedium)
                    }
                    vitalSign.spO2?.let {
                        Text("SpO₂: $it %", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    vitalSign.atemfrequenz?.let {
                        Text("AF: $it /min", style = MaterialTheme.typography.bodyMedium)
                    }
                    vitalSign.blutzucker?.let {
                        Text("BZ: $it mg/dl", style = MaterialTheme.typography.bodyMedium)
                    }
                    vitalSign.gcs?.let {
                        Text("GCS: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (vitalSign.bemerkung.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vitalSign.bemerkung,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VitalSignDialog(
    vitalSign: VitalSign?,
    onDismiss: () -> Unit,
    onSave: (VitalSign) -> Unit
) {
    var puls by remember { mutableStateOf(vitalSign?.puls) }
    var rrSys by remember { mutableStateOf(vitalSign?.rrSystolisch) }
    var rrDia by remember { mutableStateOf(vitalSign?.rrDiastolisch) }
    var spO2 by remember { mutableStateOf(vitalSign?.spO2) }
    var af by remember { mutableStateOf(vitalSign?.atemfrequenz) }
    var bz by remember { mutableStateOf(vitalSign?.blutzucker) }
    var gcs by remember { mutableStateOf(vitalSign?.gcs) }
    var schmerz by remember { mutableStateOf(vitalSign?.schmerzSkala) }
    var bemerkung by remember { mutableStateOf(vitalSign?.bemerkung ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vitalSign != null) "Messung bearbeiten" else "Neue Messung") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmsNumberField(
                        value = rrSys,
                        onValueChange = { rrSys = it },
                        label = "RR syst.",
                        suffix = "mmHg",
                        modifier = Modifier.weight(1f)
                    )
                    EmsNumberField(
                        value = rrDia,
                        onValueChange = { rrDia = it },
                        label = "RR diast.",
                        suffix = "mmHg",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmsNumberField(
                        value = puls,
                        onValueChange = { puls = it },
                        label = "Puls",
                        suffix = "/min",
                        modifier = Modifier.weight(1f)
                    )
                    EmsNumberField(
                        value = spO2,
                        onValueChange = { spO2 = it },
                        label = "SpO₂",
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        maxValue = 100
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmsNumberField(
                        value = af,
                        onValueChange = { af = it },
                        label = "AF",
                        suffix = "/min",
                        modifier = Modifier.weight(1f)
                    )
                    EmsNumberField(
                        value = bz,
                        onValueChange = { bz = it },
                        label = "BZ",
                        suffix = "mg/dl",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmsNumberField(
                        value = gcs,
                        onValueChange = { gcs = it },
                        label = "GCS",
                        modifier = Modifier.weight(1f),
                        maxValue = 15
                    )
                    EmsNumberField(
                        value = schmerz,
                        onValueChange = { schmerz = it },
                        label = "NRS",
                        modifier = Modifier.weight(1f),
                        maxValue = 10
                    )
                }
                EmsTextField(
                    value = bemerkung,
                    onValueChange = { bemerkung = it },
                    label = "Bemerkung",
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    (vitalSign ?: VitalSign(patientId = 0)).copy(
                        puls = puls,
                        rrSystolisch = rrSys,
                        rrDiastolisch = rrDia,
                        spO2 = spO2,
                        atemfrequenz = af,
                        blutzucker = bz,
                        gcs = gcs,
                        schmerzSkala = schmerz,
                        bemerkung = bemerkung
                    )
                )
            }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

private data class VitalSeries(
    val label: String,
    val color: Color,
    val values: List<Number?>
)

@Composable
private fun VitalsChart(
    vitalSigns: List<VitalSign>,
    modifier: Modifier = Modifier
) {
    val sorted = remember(vitalSigns) { vitalSigns.sortedBy { it.timestamp } }

    val allSeries = remember(sorted) {
        listOf(
            VitalSeries("RR sys", Color(0xFFE53935), sorted.map { it.rrSystolisch }),
            VitalSeries("RR dia", Color(0xFFD81B60), sorted.map { it.rrDiastolisch }),
            VitalSeries("Puls", Color(0xFF8E24AA), sorted.map { it.puls }),
            VitalSeries("SpO₂", Color(0xFF1E88E5), sorted.map { it.spO2 }),
            VitalSeries("AF", Color(0xFF43A047), sorted.map { it.atemfrequenz }),
            VitalSeries("BZ", Color(0xFFFB8C00), sorted.map { it.blutzucker }),
            VitalSeries("GCS", Color(0xFF00ACC1), sorted.map { it.gcs }),
            VitalSeries("NRS", Color(0xFF6D4C41), sorted.map { it.schmerzSkala }),
        ).filter { s -> s.values.any { it != null } }
    }

    if (allSeries.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(sorted) {
        modelProducer.runTransaction {
            lineSeries {
                allSeries.forEach { s ->
                    val yValues = s.values.map { v -> v?.toDouble() ?: 0.0 }
                    series(yValues)
                }
            }
        }
    }

    val timeLabels = remember(sorted) {
        sorted.map { DateTimeUtil.formatTime(it.timestamp) }
    }
    val bottomAxisValueFormatter = remember(timeLabels) {
        CartesianValueFormatter { _, value, _ ->
            timeLabels.getOrElse(value.toInt()) { "" }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        allSeries.map { s ->
                            LineCartesianLayer.Line(
                                fill = LineCartesianLayer.LineFill.single(fill(s.color))
                            )
                        }
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisValueFormatter
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            allSeries.forEach { s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        color = s.color,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {}
                    Text(
                        text = s.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
