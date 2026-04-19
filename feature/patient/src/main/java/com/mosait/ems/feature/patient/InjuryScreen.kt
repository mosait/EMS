package com.mosait.ems.feature.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.mosait.ems.core.ui.components.UnsavedChangesDialog

@Composable
fun InjuryScreen(
    patientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InjuryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSilhouette by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (showSilhouette) {
            showSilhouette = false
        } else if (viewModel.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            EmsTopAppBar(
                title = "Verletzung",
                onNavigateBack = handleBack,
                actions = {
                    if (uiState.selectedRegions.isNotEmpty()) {
                        IconButton(onClick = { showSilhouette = !showSilhouette }) {
                            Icon(
                                if (showSilhouette) Icons.Default.ViewList else Icons.Default.Person,
                                contentDescription = if (showSilhouette) "Liste" else "Körper"
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.save(); onNavigateBack() }) {
                        Text("Speichern")
                    }
                }
            )
        }
    ) { padding ->
        if (showSilhouette && uiState.selectedRegions.isNotEmpty()) {
            BodySilhouetteView(
                regionSeverities = uiState.regionSeverities,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
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
                // Freitext for Kopf / Hals after severity
                if (groupName == "Kopf / Hals") {
                    EmsTextField(
                        value = uiState.kopfHalsFreitext,
                        onValueChange = { viewModel.updateKopfHalsFreitext(it) },
                        label = "Kopf / Hals Beschreibung",
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
    }

    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedDialog = false },
            onSave = { viewModel.save(); onNavigateBack() },
            onDiscard = { onNavigateBack() }
        )
    }
}

private fun severityColor(severity: InjurySeverity): Color = when (severity) {
    InjurySeverity.LEICHT -> Color(0xFF66BB6A)
    InjurySeverity.MITTEL -> Color(0xFFFFA726)
    InjurySeverity.SCHWER -> Color(0xFFEF5350)
}

@Composable
private fun BodySilhouetteView(
    regionSeverities: Map<BodyRegion, InjurySeverity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verletzungsübersicht",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .width(250.dp)
                .height(450.dp)
        ) {
            val w = size.width
            val h = size.height
            val outlineColor = Color(0xFF9E9E9E)
            val outlineStroke = Stroke(width = 2f)

            // Helper: draw and optionally fill a rect region
            fun drawRegion(region: BodyRegion, left: Float, top: Float, rw: Float, rh: Float) {
                val severity = regionSeverities[region]
                if (severity != null) {
                    drawRect(
                        color = severityColor(severity).copy(alpha = 0.5f),
                        topLeft = Offset(left, top),
                        size = Size(rw, rh)
                    )
                }
                drawRect(
                    color = outlineColor,
                    topLeft = Offset(left, top),
                    size = Size(rw, rh),
                    style = outlineStroke
                )
            }

            // Head (KOPF) - circle at top center
            val headCx = w * 0.5f
            val headCy = h * 0.06f
            val headR = w * 0.09f
            regionSeverities[BodyRegion.KOPF]?.let {
                drawCircle(color = severityColor(it).copy(alpha = 0.5f), radius = headR, center = Offset(headCx, headCy))
            }
            drawCircle(color = outlineColor, radius = headR, center = Offset(headCx, headCy), style = outlineStroke)

            // Face (GESICHT) - smaller oval inside head
            regionSeverities[BodyRegion.GESICHT]?.let {
                drawOval(
                    color = severityColor(it).copy(alpha = 0.5f),
                    topLeft = Offset(headCx - headR * 0.5f, headCy - headR * 0.4f),
                    size = Size(headR, headR * 0.8f)
                )
            }

            // Neck (HALS)
            val neckTop = headCy + headR
            val neckH = h * 0.03f
            drawRegion(BodyRegion.HALS, w * 0.44f, neckTop, w * 0.12f, neckH)

            // Torso top
            val torsoTop = neckTop + neckH
            val torsoW = w * 0.36f
            val torsoLeft = w * 0.5f - torsoW * 0.5f

            // BRUST
            val brustH = h * 0.12f
            drawRegion(BodyRegion.BRUST, torsoLeft, torsoTop, torsoW, brustH)

            // BAUCH
            val bauchTop = torsoTop + brustH
            val bauchH = h * 0.10f
            drawRegion(BodyRegion.BAUCH, torsoLeft, bauchTop, torsoW, bauchH)

            // BECKEN
            val beckenTop = bauchTop + bauchH
            val beckenH = h * 0.06f
            drawRegion(BodyRegion.BECKEN, torsoLeft, beckenTop, torsoW, beckenH)

            // WIRBELSAEULE – vertical line down center of torso
            val wsX = w * 0.5f
            regionSeverities[BodyRegion.WIRBELSAEULE]?.let {
                drawLine(
                    color = severityColor(it),
                    start = Offset(wsX, torsoTop),
                    end = Offset(wsX, beckenTop),
                    strokeWidth = 6f
                )
            }
            // outline for Wirbelsäule when not affected
            if (regionSeverities[BodyRegion.WIRBELSAEULE] == null) {
                drawLine(
                    color = outlineColor.copy(alpha = 0.3f),
                    start = Offset(wsX, torsoTop),
                    end = Offset(wsX, beckenTop),
                    strokeWidth = 2f
                )
            }

            // Arms (patient perspective: patient's right = viewer's left)
            val armW = w * 0.08f
            val upperArmH = h * 0.12f
            val forearmH = h * 0.11f
            val handH = h * 0.05f

            // Right arm (patient's right = viewer's left)
            val armRX = torsoLeft - armW - w * 0.02f
            drawRegion(BodyRegion.OBERARM_RECHTS, armRX, torsoTop, armW, upperArmH)
            drawRegion(BodyRegion.UNTERARM_RECHTS, armRX, torsoTop + upperArmH, armW, forearmH)
            drawRegion(BodyRegion.HAND_RECHTS, armRX, torsoTop + upperArmH + forearmH, armW, handH)

            // Left arm (patient's left = viewer's right)
            val armLX = torsoLeft + torsoW + w * 0.02f
            drawRegion(BodyRegion.OBERARM_LINKS, armLX, torsoTop, armW, upperArmH)
            drawRegion(BodyRegion.UNTERARM_LINKS, armLX, torsoTop + upperArmH, armW, forearmH)
            drawRegion(BodyRegion.HAND_LINKS, armLX, torsoTop + upperArmH + forearmH, armW, handH)

            // RUECKEN – next to patient's left hand (viewer's right side)
            val rueckenW = w * 0.07f
            val rueckenLeft = armLX + armW + w * 0.02f
            drawRegion(BodyRegion.RUECKEN, rueckenLeft, torsoTop, rueckenW, brustH + bauchH)

            // Legs
            val legW = w * 0.12f
            val legGap = w * 0.04f
            val legTop = beckenTop + beckenH
            val thighH = h * 0.14f
            val shinH = h * 0.14f
            val footH = h * 0.04f

            // Right leg (patient's right = viewer's left)
            val legRX = w * 0.5f - legGap * 0.5f - legW
            drawRegion(BodyRegion.OBERSCHENKEL_RECHTS, legRX, legTop, legW, thighH)
            drawRegion(BodyRegion.UNTERSCHENKEL_RECHTS, legRX, legTop + thighH, legW, shinH)
            drawRegion(BodyRegion.FUSS_RECHTS, legRX, legTop + thighH + shinH, legW, footH)

            // Left leg (patient's left = viewer's right)
            val legLX = w * 0.5f + legGap * 0.5f
            drawRegion(BodyRegion.OBERSCHENKEL_LINKS, legLX, legTop, legW, thighH)
            drawRegion(BodyRegion.UNTERSCHENKEL_LINKS, legLX, legTop + thighH, legW, shinH)
            drawRegion(BodyRegion.FUSS_LINKS, legLX, legTop + thighH + shinH, legW, footH)

            // Labels for L/R
            // (Canvas doesn't support text easily, skip labels - the legend below handles it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Text("Legende", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InjurySeverity.entries.forEach { severity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(14.dp),
                        color = severityColor(severity).copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {}
                    Text(severity.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Affected regions list
        if (regionSeverities.isNotEmpty()) {
            Text("Betroffene Regionen", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            regionSeverities.forEach { (region, severity) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = region.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = severityColor(severity).copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = severity.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
