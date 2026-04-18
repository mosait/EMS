package com.mosait.ems.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ungespeicherte Änderungen") },
        text = { Text("Es gibt ungespeicherte Änderungen. Wie möchten Sie fortfahren?") },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Speichern") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Abbrechen") }
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Verwerfen") }
            }
        }
    )
}
