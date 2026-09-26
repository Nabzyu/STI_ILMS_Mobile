package com.example.stismartlib.ui.dashboard.qr
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.PrimaryBlue

private val qrPattern = listOf(
    0, 1, 2, 4, 5, 6, 9, 10, 11, 13, 15, 17, 19, 20, 22, 24, 26, 27, 29, 30,
    34, 36, 37, 39, 41, 43, 45, 47, 50, 51, 54, 56, 58, 60, 61, 64, 67, 69,
    72, 74, 76, 78, 82, 84, 86, 88, 91, 93, 95, 96, 99, 100, 104, 106, 108,
    110, 112, 114, 116, 117, 118, 120
)

@Composable
fun AttendanceScreen() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeader(
                eyebrow = "QR attendance",
                title = "Library visit pass",
                description = "Present this code at the entrance kiosk to record your visit."
            )
        }
        item {
            SectionCard(modifier = Modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Lucide.QrCode, contentDescription = null, tint = Color.White)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("STI Library Pass", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Demo QR attendance", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .background(Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(11) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                                repeat(11) { column ->
                                    val filled = qrPattern.contains(row * 11 + column)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(if (filled) PrimaryBlue else Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("John Bazty Cantay", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("02000241372 · BSIT-3A", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge("Valid for this demo session")
            }
        }
        item { StatCard("Visits this month", "12", "18.4 hours total", Lucide.LibraryBig) }
        item { StatCard("Today's check-in", "8:12 AM", "Currently in library", Lucide.Clock3) }
        item { StatCard("Most common purpose", "Research", "6 of 12 visits", Lucide.BookOpen) }
        item {
            SectionCard {
                Text("Attendance history", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                VisitRow("Aug 15, 2026", "8:12 AM", "—", "Research", true)
                VisitRow("Aug 14, 2026", "9:04 AM", "11:20 AM", "Printing", false)
                VisitRow("Aug 12, 2026", "1:15 PM", "3:02 PM", "Borrowing", false)
            }
        }
    }
}

@Composable
private fun VisitRow(date: String, timeIn: String, timeOut: String, purpose: String, active: Boolean) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(date, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("$timeIn – $timeOut · $purpose", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
        StatusBadge(if (active) "Active" else "Returned", warning = active)
    }
}
