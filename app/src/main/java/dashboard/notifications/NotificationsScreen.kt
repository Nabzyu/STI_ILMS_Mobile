package com.example.stismartlib.ui.dashboard.notifications

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.NotificationItem
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.ElmsCyanSoft
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val repository = rememberLibraryRepository()
    val notifications = repository.notifications
    val scope = rememberCoroutineScope()
    val loading = repository.isLoading && notifications.isEmpty()
    val loadError = repository.lastError.takeIf { notifications.isEmpty() }
    val unread = notifications.count { !it.isRead }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<NotificationItem?>(null) }
    var confirmClearAll by remember { mutableStateOf(false) }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete notification?") },
            text = { Text("Remove \"${pendingDelete!!.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = pendingDelete ?: return@TextButton
                        pendingDelete = null
                        busy = true
                        actionError = null
                        scope.launch {
                            try {
                                repository.deleteNotification(target.id)
                            } catch (error: Exception) {
                                actionError = userFacingMessage(error, "Unable to delete the notification.")
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("Clear all notifications?") },
            text = { Text("Every notification will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClearAll = false
                        busy = true
                        actionError = null
                        scope.launch {
                            try {
                                repository.clearAllNotifications()
                            } catch (error: Exception) {
                                actionError = userFacingMessage(error, "Unable to clear notifications.")
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text("Clear all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text("Cancel") }
            },
        )
    }

    ElmsPullRefresh(
        onRefresh = { repository.loadNotifications() },
        onRefreshError = { refreshError = it },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            if (loadError != null) {
                item {
                    ElmsBanner(message = loadError, tone = ElmsBannerTone.Error)
                }
            }
            if (refreshError != null) {
                item {
                    ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
                }
            }
            actionError?.let { message ->
                item {
                    ElmsBanner(message = message, tone = ElmsBannerTone.Error)
                }
            }
            if (unread > 0) {
                item {
                    ElmsBanner(
                        message = "You have $unread unread notification${if (unread == 1) "" else "s"}.",
                        tone = ElmsBannerTone.Info
                    )
                }
            }
            if (notifications.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        PortalButton(
                            text = "Mark all as read",
                            onClick = {
                                busy = true
                                actionError = null
                                scope.launch {
                                    try {
                                        repository.markAllNotificationsRead()
                                    } catch (error: Exception) {
                                        actionError = userFacingMessage(
                                            error,
                                            "Unable to mark notifications as read.",
                                        )
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                            variant = PortalButtonVariant.Secondary,
                            leadingIcon = Lucide.CircleCheck,
                            enabled = !busy && unread > 0,
                            modifier = Modifier.weight(1f),
                        )
                        PortalButton(
                            text = "Clear all",
                            onClick = { confirmClearAll = true },
                            variant = PortalButtonVariant.Ghost,
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            when {
                loading -> item { ElmsLoadingBlock() }
                notifications.isEmpty() -> item {
                    ElmsEmptyState(
                        title = "You're all caught up",
                        description = "New library updates will show up here."
                    )
                }
                else -> items(notifications, key = { it.id }) { item ->
                    NotificationRow(
                        item = item,
                        actionsEnabled = !busy,
                        onMarkRead = {
                            busy = true
                            actionError = null
                            scope.launch {
                                try {
                                    repository.markNotificationRead(item.id)
                                } catch (error: Exception) {
                                    actionError = userFacingMessage(
                                        error,
                                        "Unable to mark the notification as read.",
                                    )
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        onDelete = { pendingDelete = item },
                    )
                }
            }
            item {
                PortalButton(
                    text = "Back",
                    onClick = onBack,
                    variant = PortalButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    actionsEnabled: Boolean,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
) {
    ElmsSectionCard(
        contentPadding = 12.dp,
        modifier = if (item.isRead) Modifier else Modifier.background(ElmsCyanSoft)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(ShapeXl)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Lucide.Bell,
                    contentDescription = null,
                    tint = ElmsCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${item.category.label} · ${item.time}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!item.isRead) {
                        PortalButton(
                            text = "Mark read",
                            onClick = onMarkRead,
                            variant = PortalButtonVariant.Secondary,
                            enabled = actionsEnabled,
                        )
                    }
                    PortalButton(
                        text = "Delete",
                        onClick = onDelete,
                        variant = PortalButtonVariant.Ghost,
                        enabled = actionsEnabled,
                    )
                }
            }
        }
    }
}
