package com.mosait.ems.core.ui.components

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.ByteArrayOutputStream

private data class SignatureLine(
    val points: List<Offset>
)

@Composable
fun SignaturePad(
    signatureBytes: ByteArray?,
    onSignatureChanged: (ByteArray?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val existingBitmap = remember(signatureBytes) {
        signatureBytes?.let {
            try {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            } catch (_: Exception) { null }
        }
    }

    Column(modifier = modifier) {
        // Preview / tap target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .background(Color.White, MaterialTheme.shapes.medium)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (existingBitmap != null) {
                Image(
                    bitmap = existingBitmap,
                    contentDescription = "Unterschrift",
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Draw,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tippen zum Unterschreiben",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (signatureBytes != null) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = { onSignatureChanged(null) }) {
                Text("Unterschrift löschen")
            }
        }
    }

    if (showDialog) {
        SignatureDialog(
            existingBytes = signatureBytes,
            onConfirm = { bytes ->
                onSignatureChanged(bytes)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SignatureDialog(
    existingBytes: ByteArray?,
    onConfirm: (ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    val lines = remember { mutableStateListOf<SignatureLine>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var hasExistingBitmap by remember { mutableStateOf(existingBytes != null) }
    val existingBitmap = remember(existingBytes) {
        existingBytes?.let {
            try {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            } catch (_: Exception) { null }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Unterschrift",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Drawing canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        .background(Color.White, MaterialTheme.shapes.medium)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        if (hasExistingBitmap) {
                                            hasExistingBitmap = false
                                            lines.clear()
                                        }
                                        currentPoints.clear()
                                        currentPoints.add(Offset(event.x, event.y))
                                        true
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        currentPoints.add(Offset(event.x, event.y))
                                        true
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        lines.add(SignatureLine(currentPoints.toList()))
                                        currentPoints.clear()
                                        true
                                    }
                                    else -> false
                                }
                            }
                    ) {
                        canvasSize = size

                        if (hasExistingBitmap && existingBitmap != null) {
                            drawImage(existingBitmap)
                        } else {
                            val strokeColor = Color.Black
                            for (line in lines) {
                                if (line.points.size >= 2) {
                                    for (i in 0 until line.points.size - 1) {
                                        drawLine(strokeColor, line.points[i], line.points[i + 1], 3f, StrokeCap.Round)
                                    }
                                } else if (line.points.size == 1) {
                                    drawCircle(strokeColor, 1.5f, line.points[0])
                                }
                            }
                            if (currentPoints.size >= 2) {
                                for (i in 0 until currentPoints.size - 1) {
                                    drawLine(strokeColor, currentPoints[i], currentPoints[i + 1], 3f, StrokeCap.Round)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        lines.clear()
                        currentPoints.clear()
                        hasExistingBitmap = false
                    }) {
                        Text("Löschen")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("Abbrechen")
                        }
                        Button(onClick = {
                            val bytes = if (hasExistingBitmap) existingBytes
                            else rasterizeSignature(lines, canvasSize.width.toInt(), canvasSize.height.toInt())
                            onConfirm(bytes)
                        }) {
                            Text("Übernehmen")
                        }
                    }
                }
            }
        }
    }
}

private fun rasterizeSignature(lines: List<SignatureLine>, width: Int, height: Int): ByteArray? {
    if (lines.isEmpty() || width <= 0 || height <= 0) return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        isAntiAlias = true
    }
    for (line in lines) {
        if (line.points.size >= 2) {
            val path = android.graphics.Path()
            path.moveTo(line.points[0].x, line.points[0].y)
            for (i in 1 until line.points.size) {
                path.lineTo(line.points[i].x, line.points[i].y)
            }
            canvas.drawPath(path, paint)
        }
    }
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    bitmap.recycle()
    return stream.toByteArray()
}
