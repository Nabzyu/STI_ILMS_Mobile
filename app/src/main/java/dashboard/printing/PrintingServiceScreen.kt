package com.example.stismartlib.ui.dashboard.printing
import com.composables.icons.lucide.*

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.PrimaryBlue

@Composable
fun PrintingServiceScreen(onViewHistory: () -> Unit) {
    val repository = rememberLibraryRepository()
    var fileName by remember { mutableStateOf("") }
    var copies by remember { mutableStateOf("1") }
    var printType by remember { mutableStateOf("Black & white") }
    var note by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val printHistory = repository.printHistory
    val estimatedCost = calculatePrintCost(copies.toIntOrNull() ?: 1, printType == "Color")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Online service",
                title = "Printing service request",
                description = "Prepare your print job online and pay when you collect it at the library."
            )
        }
        if (submitted) {
            item {
                SectionCard {
                    Row {
                        androidx.compose.material3.Icon(Lucide.CircleCheck, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text("Request submitted", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Your request is in the pending queue. Payment is made at the library.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
        item {
            SectionCard {
                Text("New print request", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(label = "Document", value = fileName, onValueChange = { fileName = it }, placeholder = "Choose a file name", leadingIcon = Lucide.Upload)
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(label = "Print type", value = printType, onValueChange = { printType = it }, placeholder = "Black & white", leadingIcon = Lucide.Printer)
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(label = "Copies", value = copies, onValueChange = { copies = it.filter { char -> char.isDigit() }.take(3) }, placeholder = "1", leadingIcon = Lucide.FileText)
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(label = "Notes", value = note, onValueChange = { note = it }, placeholder = "Optional note", leadingIcon = Lucide.FileText)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Estimated cost ₱${"%.2f".format(estimatedCost)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("Payment is made at the library.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = "Submit request",
                    leadingIcon = Lucide.Printer,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fileName.isNotBlank(),
                    onClick = {
                        submitted = true
                        fileName = ""
                        copies = "1"
                        note = ""
                    }
                )
            }
        }
        item {
            Text("My print history", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        items(printHistory, key = { it.id }) { request ->
            PrintQueueCard(request)
        }
        item {
            PortalButton(text = "Open full history", onClick = onViewHistory, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PrintQueueCard(request: PrintRequest) {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.fileName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text("${request.id} · ${request.copies} copies · ${request.printType}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontSize = 12.sp)
            }
            StatusBadge(request.status.label, warning = request.status == PrintStatus.PENDING || request.status == PrintStatus.PRINTING)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("₱${"%.2f".format(request.totalCost)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text("Pay at library", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), fontSize = 11.sp)
    }
}

private fun calculatePrintCost(copies: Int, isColored: Boolean): Double {
    val rate = if (isColored) 5.0 else 2.0
    return copies * rate
}
