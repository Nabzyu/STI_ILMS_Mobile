package com.example.stismartlib.ui.dashboard.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.CircleDollarSign
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.ElmsPullRefresh
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.ErrorRed
import com.example.stismartlib.ui.theme.StiBrandBlue

@Composable
fun ClearanceScreen() {
    val repository = rememberLibraryRepository()
    val standing = repository.clearance
    var refreshError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { repository.loadClearance() }
    }

    ElmsPullRefresh(
        onRefresh = { repository.loadClearance() },
        onRefreshError = { refreshError = it },
    ) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(
                eyebrow = "Account standing",
                title = "Library clearance",
                description = "Official standing from the library clearance service.",
            )
        }
        standing?.scheduleLine?.let { line ->
            item {
                ElmsBanner(
                    message = line,
                    tone = if (standing.pastDeadline && !standing.isCleared) ElmsBannerTone.Error else ElmsBannerTone.Info,
                )
            }
        }
        if (refreshError != null) {
            item {
                ElmsBanner(
                    message = refreshError.orEmpty(),
                    tone = ElmsBannerTone.Error,
                )
            }
        }
        if (repository.clearanceError != null) {
            item {
                ElmsBanner(
                    message = repository.clearanceError.orEmpty(),
                    tone = ElmsBannerTone.Error,
                )
            }
        }
        if (repository.isClearanceLoading && standing == null) {
            item { ElmsLoadingBlock() }
        }
        if (standing != null) {
            val bannerColor = if (standing.isCleared) StiBrandBlue else ErrorRed
            val bannerIcon = if (standing.isCleared) Lucide.BadgeCheck else Lucide.ShieldAlert
            val bannerIconTint = if (standing.isCleared) AccentYellow else Color.White
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(bannerColor)
                        .padding(24.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            bannerIcon,
                            contentDescription = null,
                            tint = bannerIconTint,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            " ${standing.statusLabel.uppercase()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (standing.isCleared) {
                            "You have no library obligations."
                        } else {
                            "Your clearance is currently blocked."
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        standing.reason.ifBlank {
                            if (standing.isCleared) {
                                "This status comes from the library clearance service."
                            } else {
                                "Return unreturned books and settle unpaid charges at the library."
                            }
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                    )
                    standing.activeOverrideReason?.takeIf { it.isNotBlank() }?.let { overrideReason ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Active override: $overrideReason",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            item {
                StatCard(
                    "Unreturned books",
                    standing.activeLoans.toString(),
                    icon = Lucide.BookOpen,
                    note = null,
                )
            }
            item {
                StatCard(
                    "Outstanding fines",
                    "₱${"%.2f".format(standing.totalOutstanding)}",
                    icon = Lucide.CircleDollarSign,
                    note = "${standing.unpaidOverdueFines} overdue fine item(s)",
                )
            }
            item {
                StatCard(
                    "Lost-book charges",
                    "₱${"%.2f".format(standing.unpaidReplacementCharges)}",
                    icon = Lucide.ShieldAlert,
                    note = null,
                )
            }
            item {
                SectionCard {
                    Text(
                        "Unreturned books",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (standing.loans.isEmpty()) {
                        Text(
                            "No unreturned books.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    } else {
                        standing.loans.forEach { loan ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    loan.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "Due ${loan.dueAt ?: "—"}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontSize = 12.sp,
                                )
                                StatusBadge(
                                    loan.status.ifBlank { "On loan" },
                                    warning = loan.status.contains("overdue", ignoreCase = true),
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Text(
                        "Outstanding charges",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (standing.fines.isEmpty() && standing.lostBooks.isEmpty()) {
                        Text(
                            "No unpaid charges.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    } else {
                        standing.fines.forEach { fine ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    fine.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    "₱${"%.2f".format(fine.amount)}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        standing.lostBooks.forEach { lost ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        lost.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "${lost.status} · ${lost.paymentStatus}",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        fontSize = 12.sp,
                                    )
                                }
                                Text(
                                    "₱${"%.2f".format(lost.replacementCharge)}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Row {
                        Icon(
                            Lucide.Clock3,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                "How clearance is determined",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Any unreturned book or unpaid fine blocks clearance. Authorized staff may record a reasoned override.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }
    }
}
