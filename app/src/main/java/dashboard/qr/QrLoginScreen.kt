package com.example.stismartlib.ui.dashboard.qr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType
import com.example.stismartlib.ui.components.PrimaryButton
import com.example.stismartlib.ui.dashboard.components.ProcessStepCard
import com.example.stismartlib.ui.dashboard.components.QrScannerPlaceholder
import com.example.stismartlib.ui.dashboard.components.SectionCard
import com.example.stismartlib.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrLoginScreen(
    onBack: () -> Unit,
    isAuthFlow: Boolean = false,
    onAuthSuccess: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val isCheckIn = selectedTab == 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAuthFlow) "QR Login" else "QR Check-In / Check-Out") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isAuthFlow) {
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Check In") },
                            icon = { Icon(Icons.Outlined.Login, contentDescription = null) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Check Out") },
                            icon = { Icon(Icons.Outlined.Logout, contentDescription = null) }
                        )
                    }
                }
            }

            item {
                QrScannerPlaceholder(
                    hint = if (isCheckIn) {
                        "Scan the library QR code to check in"
                    } else {
                        "Scan the exit QR code to check out"
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9DB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isCheckIn) "Check-In Instructions" else "Check-Out Instructions",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isCheckIn) {
                                "Point your camera at the QR code displayed at the library entrance. " +
                                    "This logs your library visit and enables borrowing services."
                            } else {
                                "Scan the QR code at the library exit to log out of your session " +
                                    "and record your departure time."
                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            item {
                SectionCard(title = if (isCheckIn) "Check-In Steps" else "Check-Out Steps") {
                    if (isCheckIn) {
                        ProcessStepCard(
                            stepNumber = 1,
                            title = "Open QR Scanner",
                            description = "Tap 'Open Scanner' and allow camera access when prompted.",
                            isActive = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProcessStepCard(
                            stepNumber = 2,
                            title = "Scan Entrance QR",
                            description = "Align the QR code within the scanner frame."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProcessStepCard(
                            stepNumber = 3,
                            title = "Session Active",
                            description = "You are checked in and can use library services."
                        )
                    } else {
                        ProcessStepCard(
                            stepNumber = 1,
                            title = "Return All Books",
                            description = "Make sure all borrowed books are returned first.",
                            isActive = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProcessStepCard(
                            stepNumber = 2,
                            title = "Scan Exit QR",
                            description = "Scan the QR code at the library exit gate."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProcessStepCard(
                            stepNumber = 3,
                            title = "Session Ended",
                            description = "Your library session is logged out successfully."
                        )
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Simulate QR Scan (No Camera)",
                    onClick = { showSuccessDialog = true },
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    if (isAuthFlow) "Login Successful"
                    else if (isCheckIn) "Check-In Successful"
                    else "Check-Out Successful"
                )
            },
            text = {
                Text(
                    if (isAuthFlow) {
                        "QR login verified. Welcome to STI Smart Library!\n\n(UI preview — no backend action yet.)"
                    } else if (isCheckIn) {
                        "You are now checked in at STI Library.\n\n(UI preview — no backend action yet.)"
                    } else {
                        "You have been checked out successfully.\n\n(UI preview — no backend action yet.)"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    if (isAuthFlow) onAuthSuccess?.invoke()
                }) { Text("Continue") }
            }
        )
    }
}