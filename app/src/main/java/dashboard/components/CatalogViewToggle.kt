package com.example.stismartlib.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.LayoutList
import com.composables.icons.lucide.Lucide
import com.example.stismartlib.data.models.Book
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.ElmsCyanSoft

@Composable
fun CatalogViewToggle(
    mode: CatalogViewMode,
    onModeChange: (CatalogViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatalogViewToggleButton(
            icon = Lucide.LayoutList,
            contentDescription = "List view",
            selected = mode == CatalogViewMode.LIST,
            onClick = { onModeChange(CatalogViewMode.LIST) }
        )
        CatalogViewToggleButton(
            icon = Lucide.LayoutGrid,
            contentDescription = "Grid view",
            selected = mode == CatalogViewMode.GRID_2,
            onClick = { onModeChange(CatalogViewMode.GRID_2) }
        )
    }
}

@Composable
private fun CatalogViewToggleButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(ShapeXl)
            .background(
                if (selected) ElmsCyan.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) ElmsCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun BookCatalogGridTile(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = book.availableCopies > 0
    ElmsSectionCard(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = 10.dp
    ) {
        BookCoverPlaceholder(
            title = book.title,
            color = Color(book.coverColor),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        StatusBadge(
            label = if (isAvailable) "Available" else "Reserved",
            warning = !isAvailable
        )
    }
}

@Composable
fun ResearchCatalogGridTile(
    item: Research,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElmsSectionCard(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(ShapeXl)
                .background(ElmsCyanSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.FileText,
                contentDescription = null,
                tint = ElmsCyan,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${item.yearPublished} · ${item.category}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
