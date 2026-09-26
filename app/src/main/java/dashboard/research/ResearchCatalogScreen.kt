package com.example.stismartlib.ui.dashboard.research
import com.composables.icons.lucide.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.Research
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun ResearchCatalogScreen(onResearchClick: (String) -> Unit) {
    val repository = rememberLibraryRepository()
    var query by remember { mutableStateOf("") }
    val rows = repository.research.filter {
        "${it.title} ${it.author} ${it.category}".contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Research repository",
                title = "Research and thesis catalog",
                description = "Discover institutional studies by department, author, title, or year.",
                action = {
                    PortalButton(text = "APA reference guide", onClick = {}, leadingIcon = Lucide.FileText)
                }
            )
        }
        item {
            PortalField(
                label = "",
                value = query,
                onValueChange = { query = it },
                placeholder = "Search research...",
                leadingIcon = Lucide.Search
            )
        }
        items(rows, key = { it.id }) { item ->
            ResearchListItem(item, onClick = { onResearchClick(item.id) })
        }
    }
}

@Composable
fun ResearchListItem(item: Research, onClick: () -> Unit) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(item.author, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(item.category, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Text("${item.yearPublished} · ${item.shelfLocation}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
            }
            StatusBadge("Available")
        }
        Spacer(modifier = Modifier.height(8.dp))
        PortalButton(text = "View", onClick = onClick, variant = PortalButtonVariant.Ghost)
    }
}
