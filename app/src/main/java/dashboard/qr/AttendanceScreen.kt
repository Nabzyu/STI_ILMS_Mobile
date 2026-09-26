package com.example.stismartlib.ui.dashboard.qr
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.theme.PrimaryBlue
import com.example.stismartlib.ui.theme.PrimaryBlue5
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AttendanceScreen() {
    val repository = rememberLibraryRepository()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "QR attendance",
                title = "Library visit pass",
                description = "Download this permanent QR code once and keep it on your phone. It works even when your device has no internet connection."
            )
        }
        item {
            SectionCard(contentPadding = 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(ShapeXl)
                            .background(Color.White.copy(alpha = 0.10f))
                            .padding(8.dp)
                    ) {
                        Icon(Lucide.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("STI Library Pass", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Permanent offline attendance QR", color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .aspectRatio(1f)
                            .clip(ShapeXl)
                            .background(PrimaryBlue5)
                            .border(1.dp, MaterialTheme.colorScheme.outline, ShapeXl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Lucide.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "QR code unavailable",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        repository.session.userName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Sign in to load your library pass",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Visits this month", "0", null, Lucide.LibraryBig, modifier = Modifier.weight(1f))
                StatCard("Today's check-in", "—", null, Lucide.Clock3, modifier = Modifier.weight(1f))
            }
        }
        item {
            StatCard("Most common purpose", "—", "No visit history", Lucide.BookOpen)
        }
        item {
            SectionCard(contentPadding = 0.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Attendance history",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Your recent library visits",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                    Text(
                    "No attendance records yet.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
