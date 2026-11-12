package com.example.stismartlib.ui.dashboard.schedule

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.DayHours
import com.example.stismartlib.data.LibrarySchedule
import com.example.stismartlib.data.campusToday
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.ShapeXl
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ClosureDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)

private fun DayOfWeek.label(): String = getDisplayName(TextStyle.FULL, Locale.US)

/** Dashboard card: today's hours, the next posted closure, and a link to the floor plan. */
@Composable
fun LibraryHoursCard(
    schedule: LibrarySchedule,
    onViewMap: () -> Unit,
    today: LocalDate = campusToday(),
) {
    val todayHours = schedule.hoursFor(today.dayOfWeek)
    val nextClosure = schedule.closures.firstOrNull { !it.date.isBefore(today) }
    SectionCard {
        Text("Hours and schedule", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Lucide.Clock3, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Today (${today.dayOfWeek.label()}): ${todayHours?.label ?: "Hours not posted"}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    nextClosure?.let { "Next closure: ${it.date.format(ClosureDate)} · ${it.reason}" }
                        ?: "No upcoming closure is posted.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        PortalButton(
            text = "View library map",
            onClick = onViewMap,
            variant = PortalButtonVariant.Secondary,
            leadingIcon = Lucide.MapPin,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Notifications section: the full week and every posted closure. */
@Composable
fun LibraryScheduleSection(schedule: LibrarySchedule, today: LocalDate = campusToday()) {
    SectionCard {
        Text("Library schedule", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Asia/Manila campus time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        if (schedule.weekly.isEmpty()) {
            Text("Opening hours are not posted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        } else {
            schedule.weekly.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { day -> DayTile(day, isToday = day.day == today.dayOfWeek, modifier = Modifier.weight(1f)) }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Upcoming closures", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val closures = schedule.closures.filter { !it.date.isBefore(today) }
        if (closures.isEmpty()) {
            Text("No upcoming closures are recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        } else {
            closures.forEach { closure ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(closure.date.format(ClosureDate), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(closure.reason, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DayTile(day: DayHours, isToday: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(ShapeXl)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (isToday) 0.12f else 0.05f))
            .padding(12.dp)
    ) {
        Text(
            if (isToday) "${day.day.label()} · Today" else day.day.label(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        Text(day.label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}
