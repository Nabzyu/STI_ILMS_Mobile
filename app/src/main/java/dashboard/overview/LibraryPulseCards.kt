package com.example.stismartlib.ui.dashboard.overview

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stismartlib.data.DashboardSummary
import com.example.stismartlib.data.LibraryAnnouncement
import com.example.stismartlib.data.occupancyPercent
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.theme.EyebrowStyle

/** Web dashboard's live facts: library occupancy and, when published, the library announcement. */
@Composable
fun LibraryPulseCards(summary: DashboardSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OccupancyCard(summary.occupancyCurrent, summary.occupancyCapacity)
        summary.announcement?.let { AnnouncementCard(it) }
    }
}

@Composable
private fun OccupancyCard(current: Int, capacity: Int) {
    val description = "Library occupancy: $current of $capacity seats in use"
    SectionCard(modifier = Modifier.clearAndSetSemantics { contentDescription = description }) {
        Text("Library occupancy", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$current / $capacity",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { occupancyPercent(current, capacity) / 100f },
            // The M3 default track (secondaryContainer) is near-invisible in this theme.
            color = MaterialTheme.colorScheme.onSurface,
            trackColor = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AnnouncementCard(announcement: LibraryAnnouncement) {
    SectionCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Lucide.Bell, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Library announcement · ${announcement.priority}".uppercase(),
                    style = EyebrowStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(announcement.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                if (announcement.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(announcement.message, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                }
                if (announcement.publishedAt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(announcement.publishedAt, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
