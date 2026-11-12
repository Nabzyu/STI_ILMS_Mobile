package com.example.stismartlib.ui.dashboard.history

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.InLibraryUse
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsEmptyState
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.format.formatDateTime

/** "My in-library use": copies used on site, assigned at the desk (`GET borrowing/in-library`). */
fun LazyListScope.inLibraryUseItems(uses: List<InLibraryUse>, error: String?) {
    item {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "My in-library use",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    when {
        error != null && uses.isEmpty() -> item { ElmsBanner(message = error, tone = ElmsBannerTone.Error) }
        uses.isEmpty() -> item {
            ElmsEmptyState(
                title = "No in-library use",
                description = "In-library assignments are separate from take-home loans.",
            )
        }
        else -> items(uses, key = { "in-library-${it.id}" }) { use ->
            ElmsSectionCard(contentPadding = 12.dp) {
                Text(use.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    listOf(use.barcode, use.statusLabel).filter { it.isNotBlank() }.joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
                if (use.handBackBy.isNotBlank()) {
                    Text(
                        "Hand back: ${formatDateTime(use.handBackBy)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
