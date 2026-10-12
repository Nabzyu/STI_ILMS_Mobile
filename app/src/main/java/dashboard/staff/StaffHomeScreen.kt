package com.example.stismartlib.ui.dashboard.staff

import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.ScanLine
import com.composables.icons.lucide.ShieldCheck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.theme.ElmsCyan

@Composable
fun StaffHomeScreen(onLogout: () -> Unit) {
    val repository = rememberLibraryRepository()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sign out?") },
            text = { Text("You will need to sign in again to use staff tools.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    }
                ) { Text("Sign out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Staff workspace",
                title = "Librarian / Admin",
                description = "Signed in as ${repository.session.userName.ifBlank { repository.session.schoolId }}."
            )
        }
        item {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = ElmsCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "  Admin session active",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Counter tools will appear here. Inventory scan, borrow/return, and attendance are next.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        item {
            Text(
                "Coming next",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        item {
            UpcomingToolRow(
                icon = Lucide.ScanLine,
                title = "Inventory barcode scan",
                subtitle = "Verify book and research copies"
            )
        }
        item {
            UpcomingToolRow(
                icon = Lucide.BadgeCheck,
                title = "Borrow / return confirm",
                subtitle = "Complete counter checkout and returns"
            )
        }
        item {
            UpcomingToolRow(
                icon = Lucide.QrCode,
                title = "Attendance scan",
                subtitle = "Check students in and out"
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PortalButton(
                text = "Sign out",
                leadingIcon = Lucide.LogOut,
                variant = PortalButtonVariant.Secondary,
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UpcomingToolRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = ElmsCyan, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
            Text(
                "Soon",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
