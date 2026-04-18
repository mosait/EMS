package com.mosait.ems.feature.overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.model.MissionStatus
import com.mosait.ems.core.ui.theme.StatusCompleted
import com.mosait.ems.core.ui.theme.StatusDraft
import com.mosait.ems.core.ui.theme.StatusExported
import com.mosait.ems.core.ui.theme.StatusInProgress

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MissionCard(
    mission: Mission,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val statusColor = when (mission.status) {
        MissionStatus.DRAFT -> StatusDraft
        MissionStatus.IN_PROGRESS -> StatusInProgress
        MissionStatus.COMPLETED -> StatusCompleted
        MissionStatus.EXPORTED -> StatusExported
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (mission.einsatzNummer.isNotBlank())
                        "Einsatz ${mission.einsatzNummer}"
                    else
                        "Einsatz #${mission.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${mission.einsatzArt.name} • ${mission.rettungsMittel.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mission.einsatzOrtOrt.isNotBlank()) {
                    Text(
                        text = mission.einsatzOrtOrt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeUtil.formatDate(mission.einsatzDatum),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = mission.status.displayName,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
