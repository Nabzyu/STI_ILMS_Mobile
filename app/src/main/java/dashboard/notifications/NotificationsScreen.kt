package com.example.stismartlib.ui.dashboard.notifications

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsListRow
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.ElmsCyanSoft

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    val items = repository.notifications
    val unread = items.count { !it.isRead }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Due dates, reservations, printing, and announcements",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        if (unread > 0) {
            item {
                ElmsBanner(
                    message = "You have $unread unread notification${if (unread == 1) "" else "s"}.",
                    tone = ElmsBannerTone.Info
                )
            }
        }
        if (items.isNotEmpty()) {
            item {
                PortalButton(
                    text = "Mark all as read",
                    onClick = {
                        val updated = items.map { it.copy(isRead = true) }
                        items.clear()
                        items.addAll(updated)
                    },
                    variant = PortalButtonVariant.Secondary,
                    leadingIcon = Lucide.CircleCheck
                )
            }
        }
        if (items.isEmpty()) {
            item {
                ElmsEmptyState(
                    title = "You're all caught up",
                    description = "New library updates will show up here."
                )
            }
        } else {
            items(items, key = { it.id }) { item ->
                NotificationRow(item)
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    ElmsSectionCard(
        contentPadding = 8.dp,
        modifier = if (item.isRead) Modifier else Modifier.background(ElmsCyanSoft)
    ) {
        ElmsListRow(
            title = item.title,
            subtitle = "${item.message}\n${item.category.label} · ${item.time}",
            leading = {
                Icon(
                    Lucide.Bell,
                    contentDescription = null,
                    tint = ElmsCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        )
    }
}
