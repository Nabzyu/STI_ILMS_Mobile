package com.example.stismartlib.ui.dashboard.research

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.apaCitation
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.dashboard.components.BookCoverPlaceholder
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun ResearchDetailScreen(
    researchId: String,
    onBack: () -> Unit
) {
    val repository = rememberLibraryRepository()
    var showApaCitation by remember { mutableStateOf(false) }
    val item = repository.research.find { it.id == researchId }

    if (item == null) {
        Column(modifier = Modifier.fillMaxSize().then(Modifier)) {
            Text("Research not found.", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.height(48.dp))
            PortalButton(text = "Go back", onClick = onBack, variant = PortalButtonVariant.Secondary)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            PageHeader(eyebrow = "Research repository", title = item.title, description = item.author)
            Spacer(modifier = Modifier.height(12.dp))
            BookCoverPlaceholder(title = item.title, color = Color(item.coverColor), modifier = Modifier.height(120.dp).fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            StatusBadge("View only", warning = true)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionCard {
                Text("Department", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(item.category, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Year", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(item.yearPublished.toString(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Shelf", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(item.shelfLocation, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.abstract, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
            PortalButton(text = "Generate APA reference", onClick = { showApaCitation = true }, modifier = Modifier.fillMaxWidth())
        }
    }

    AppDialog(
        visible = showApaCitation,
        onDismiss = { showApaCitation = false },
        type = AppDialogType.INFO,
        title = "APA Citation",
        message = item.apaCitation(),
        confirmText = "Close"
    )
}
