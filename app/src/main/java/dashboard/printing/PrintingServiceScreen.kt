package com.example.stismartlib.ui.dashboard.printing

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Printer
import com.composables.icons.lucide.Upload
import com.example.stismartlib.data.models.PrintQuote
import com.example.stismartlib.data.models.PrintRequest
import com.example.stismartlib.data.models.PrintStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val QUOTE_DEBOUNCE_MS = 250L
private val PRINT_TYPES = listOf("Monochrome", "Colored")
private val PAPER_SIZES = listOf("Short", "A4", "Long")

@Composable
fun PrintingServiceScreen(onViewHistory: () -> Unit) {
    val context = LocalContext.current
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    val printHistory = repository.printHistory

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedMime by remember { mutableStateOf("application/pdf") }
    var selectedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var copies by remember { mutableStateOf("1") }
    var printType by remember { mutableStateOf(PRINT_TYPES.first()) }
    var paperSize by remember { mutableStateOf(PAPER_SIZES.first()) }
    var notes by remember { mutableStateOf("") }
    var quote by remember { mutableStateOf<PrintQuote?>(null) }
    var quoting by remember { mutableStateOf(false) }
    var quoteError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedUri = uri
        selectedName = queryDisplayName(context.contentResolver, uri) ?: "document.pdf"
        selectedMime = context.contentResolver.getType(uri) ?: "application/pdf"
        selectedBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        quote = null
        quoteError = null
        successMessage = null
    }

    LaunchedEffect(Unit) {
        runCatching { repository.loadPrintServiceStatus() }
        runCatching { repository.loadPrintHistory() }
    }

    LaunchedEffect(selectedBytes, copies, printType, paperSize, repository.printServiceAccepting) {
        val bytes = selectedBytes
        val name = selectedName
        quote = null
        quoteError = null
        if (bytes == null || name.isNullOrBlank() || !repository.printServiceAccepting) return@LaunchedEffect
        val copyCount = copies.toIntOrNull()
        if (copyCount == null || copyCount !in 1..50) {
            quoteError = "Enter 1–50 copies."
            return@LaunchedEffect
        }
        delay(QUOTE_DEBOUNCE_MS)
        quoting = true
        try {
            quote = repository.quotePrint(
                fileBytes = bytes,
                fileName = name,
                mimeType = selectedMime,
                copies = copyCount,
                printType = printType,
                paperSize = paperSize,
            )
        } catch (error: Exception) {
            quoteError = userFacingMessage(error, "Unable to read the document.")
        } finally {
            quoting = false
        }
    }

    val accepting = repository.printServiceAccepting
    val canSubmit = accepting &&
        selectedBytes != null &&
        quote != null &&
        !quoting &&
        !repository.isPrintSubmitting

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(
                eyebrow = "Online service",
                title = "Printing service request",
                description = "Upload a PDF or DOCX, confirm the server price, then pay cash at the library counter.",
            )
        }
        if (!accepting) {
            item {
                ElmsBanner(
                    message = repository.printUnavailableReason
                        ?.takeIf { it.isNotBlank() }
                        ?: "The printing service is not accepting requests right now.",
                    tone = ElmsBannerTone.Info,
                )
            }
        }
        formError?.let { message ->
            item { ElmsBanner(message = message, tone = ElmsBannerTone.Error) }
        }
        repository.printActionError?.let { message ->
            item { ElmsBanner(message = message, tone = ElmsBannerTone.Error) }
        }
        successMessage?.let { message ->
            item {
                SectionCard {
                    Row {
                        androidx.compose.material3.Icon(
                            Lucide.CircleCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                "Request submitted",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                message,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
        item {
            SectionCard {
                Text(
                    "New print request",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = selectedName ?: "Choose PDF or DOCX",
                    leadingIcon = Lucide.Upload,
                    variant = PortalButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val mimeTypes = if (repository.printDocxAutoCountAvailable) {
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            )
                        } else {
                            arrayOf("application/pdf")
                        }
                        picker.launch(mimeTypes)
                    },
                )
                Text(
                    if (repository.printDocxAutoCountAvailable) {
                        "PDF or DOCX, maximum 4 MB"
                    } else {
                        "PDF only while automatic DOCX counting is unavailable, maximum 4 MB"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(
                    label = "Copies",
                    value = copies,
                    onValueChange = { copies = it.filter(Char::isDigit).take(2) },
                    placeholder = "1",
                    leadingIcon = Lucide.FileText,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionChips(
                    label = "Print type",
                    options = PRINT_TYPES,
                    selected = printType,
                    onSelect = { printType = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionChips(
                    label = "Paper size",
                    options = PAPER_SIZES,
                    selected = paperSize,
                    onSelect = { paperSize = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                PortalField(
                    label = "Notes (optional)",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Optional note",
                    leadingIcon = Lucide.FileText,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Pages in document",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Text(
                    when {
                        quoting -> "Counting…"
                        quote != null -> quote!!.pageCount.toString()
                        else -> "—"
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                if (quoteError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(quoteError.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Calculated cost",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Text(
                    quote?.let { "₱${"%.2f".format(it.calculatedCost)}" } ?: "—",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                )
                Text(
                    quote?.let { "${it.totalSheets} total printed sheets · Cash payment at the library counter" }
                        ?: "Cash payment at the library counter",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                PortalButton(
                    text = if (repository.isPrintSubmitting) "Submitting…" else "Submit print request",
                    leadingIcon = Lucide.Printer,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit,
                    loading = repository.isPrintSubmitting,
                    onClick = {
                        val bytes = selectedBytes ?: return@PortalButton
                        val name = selectedName ?: return@PortalButton
                        val activeQuote = quote ?: return@PortalButton
                        val copyCount = copies.toIntOrNull() ?: return@PortalButton
                        formError = null
                        successMessage = null
                        scope.launch {
                            try {
                                repository.submitPrint(
                                    fileBytes = bytes,
                                    fileName = name,
                                    mimeType = selectedMime,
                                    copies = copyCount,
                                    printType = printType,
                                    paperSize = paperSize,
                                    quote = activeQuote,
                                    notes = notes,
                                )
                                selectedUri = null
                                selectedName = null
                                selectedBytes = null
                                quote = null
                                notes = ""
                                copies = "1"
                                successMessage =
                                    "Your request is in the pending queue. Pay cash at the library counter before printing starts."
                            } catch (error: ApiException) {
                                formError = userFacingMessage(error, "Unable to submit the print request.")
                            } catch (error: Exception) {
                                formError = userFacingMessage(error, "Unable to submit the print request.")
                            }
                        }
                    },
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Recent requests",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                PortalButton(
                    text = "View history",
                    variant = PortalButtonVariant.Ghost,
                    onClick = onViewHistory,
                )
            }
        }
        items(printHistory.take(5), key = { it.id }) { request ->
            PrintQueueCard(request)
        }
    }
}

@Composable
private fun OptionChips(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            PortalButton(
                text = option,
                variant = if (option == selected) {
                    PortalButtonVariant.Primary
                } else {
                    PortalButtonVariant.Secondary
                },
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
fun PrintQueueCard(request: PrintRequest) {
    SectionCard {
        Text(request.fileName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Text(
            "${request.pageCount.takeIf { it > 0 } ?: "—"} pages · ${request.copies} copies · ${request.printType} · ${request.note}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(request.paymentStatus.ifBlank { "—" })
            StatusBadge(
                request.status.label,
                warning = request.status == PrintStatus.PENDING || request.status == PrintStatus.PRINTING,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "₱${"%.2f".format(request.totalCost)} · ${request.dateSubmitted}",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}
