package com.example.stismartlib.ui.dashboard.notifications
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    var items by remember { mutableStateOf(repository.notifications) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeader(
                eyebrow = "Activity center",
                title = "Notifications",
                description = "Due-date reminders, reservation alerts, printing updates, and library announcements.",
                action = {
                    if (items.isNotEmpty()) {
                        PortalButton(
                            text = "Mark all as read",
                            onClick = { items = items.map { it.copy(isRead = true) } },
                            variant = PortalButtonVariant.Secondary,
                            leadingIcon = Lucide.CircleCheck
                        )
                    }
                }
            )
        }
        if (items.isEmpty()) {
            item {
                SectionCard {
                    Text(
                        "No notifications yet.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }
        } else {
            item {
                SectionCard {
                    items.forEach { item ->
                        NotificationRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.isRead) Color.Transparent else PrimaryBlue.copy(alpha = 0.04f))
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Lucide.Bell,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryBlue.copy(alpha = if (item.isRead) 0.05f else 0.10f))
                .padding(10.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(item.message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 14.sp)
            Text("${item.category.label} · ${item.time}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp)
        }
    }
}
