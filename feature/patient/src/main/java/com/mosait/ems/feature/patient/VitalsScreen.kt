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
private fun VitalValueRow(label: String, value: String, unit: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = if (unit.isNotBlank()) "$value $unit" else value,
            style = MaterialTheme.typography.bodyMedium
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

            // Hämodynamik
            val hasHaemo = vitalSign.rrSystolisch != null || vitalSign.puls != null || vitalSign.spO2 != null
            if (hasHaemo) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Hämodynamik", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                vitalSign.rrSystolisch?.let { sys ->
                    val dias = vitalSign.rrDiastolisch?.let { "/$it" } ?: ""
                    VitalValueRow("Blutdruck", "$sys$dias", "mmHg")
                }
                vitalSign.puls?.let { VitalValueRow("Puls", "$it", "/min") }
                vitalSign.spO2?.let { VitalValueRow("SpO₂", "$it", "%") }
            }

            // Atmung & Stoffwechsel
            val hasAtmung = vitalSign.atemfrequenz != null || vitalSign.blutzucker != null || vitalSign.temperatur != null
            if (hasAtmung) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Atmung & Stoffwechsel", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                vitalSign.atemfrequenz?.let { VitalValueRow("Atemfrequenz", "$it", "/min") }
                vitalSign.blutzucker?.let { VitalValueRow("Blutzucker", "$it", "mg/dl") }
                vitalSign.temperatur?.let { VitalValueRow("Temperatur", "$it", "°C") }
            }

            // Neurologie
            val hasNeuro = vitalSign.gcs != null || vitalSign.schmerzSkala != null || vitalSign.ekg != null
            if (hasNeuro) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Neurologie & Monitoring", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                vitalSign.gcs?.let { VitalValueRow("GCS", "$it") }
                vitalSign.schmerzSkala?.let { VitalValueRow("Schmerz (NRS)", "$it") }
                vitalSign.ekg?.let { VitalValueRow("EKG", it.name.replace("_", " ")) }
            }

            if (vitalSign.bemerkung.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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

private data class VitalChartGroup(
    val title: String,
    val unit: String,
    val series: List<VitalSeries>
)

@Composable
private fun SingleGroupChart(
    group: VitalChartGroup,
    timeLabels: List<String>,
    modifier: Modifier = Modifier
) {
    val activeSeries = group.series.filter { s -> s.values.any { it != null } }
    if (activeSeries.isEmpty()) return

    val modelProducer = remember(activeSeries) { CartesianChartModelProducer() }

    LaunchedEffect(activeSeries) {
        modelProducer.runTransaction {
            lineSeries {
                activeSeries.forEach { s ->
                    series(s.values.map { v -> v?.toDouble() ?: 0.0 })
                }
            }
        }
    }

    val bottomAxisValueFormatter = remember(timeLabels) {
        CartesianValueFormatter { _, value, _ ->
            timeLabels.getOrElse(value.toInt()) { "" }
        }
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${group.title} (${group.unit})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            activeSeries.map { s ->
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
                    .height(180.dp),
            )

            if (activeSeries.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    activeSeries.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(10.dp),
                                color = s.color,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {}
                            Text(s.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalsChart(
    vitalSigns: List<VitalSign>,
    modifier: Modifier = Modifier
) {
    val sorted = remember(vitalSigns) { vitalSigns.sortedBy { it.timestamp } }

    val timeLabels = remember(sorted) {
        sorted.map { DateTimeUtil.formatTime(it.timestamp) }
    }

    val chartGroups = remember(sorted) {
        listOf(
            VitalChartGroup(
                title = "Hämodynamik",
                unit = "mmHg / /min / %",
                series = listOf(
                    VitalSeries("RR sys", Color(0xFFE53935), sorted.map { it.rrSystolisch }),
                    VitalSeries("RR dia", Color(0xFFD81B60), sorted.map { it.rrDiastolisch }),
                    VitalSeries("Puls", Color(0xFF8E24AA), sorted.map { it.puls }),
                    VitalSeries("SpO₂", Color(0xFF1E88E5), sorted.map { it.spO2 }),
                )
            ),
            VitalChartGroup(
                title = "Atmung",
                unit = "/min",
                series = listOf(
                    VitalSeries("AF", Color(0xFF43A047), sorted.map { it.atemfrequenz }),
                )
            ),
            VitalChartGroup(
                title = "Stoffwechsel",
                unit = "mg/dl / °C",
                series = listOf(
                    VitalSeries("BZ", Color(0xFFFB8C00), sorted.map { it.blutzucker }),
                    VitalSeries("Temp", Color(0xFFFF7043), sorted.map { it.temperatur }),
                )
            ),
        ).filter { group -> group.series.any { s -> s.values.any { it != null } } }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chartGroups.forEach { group ->
            SingleGroupChart(
                group = group,
                timeLabels = timeLabels
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
