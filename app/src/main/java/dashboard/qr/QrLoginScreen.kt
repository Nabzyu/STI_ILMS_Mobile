package com.example.stismartlib.ui.dashboard.qr

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stismartlib.ui.components.ElmsPillTab
import com.example.stismartlib.ui.components.ElmsSectionCard
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.dashboard.components.QrScannerPlaceholder
import com.example.stismartlib.ui.theme.ElmsCyan

@Composable
fun QrLoginScreen(
    onBack: () -> Unit,
    isAuthFlow: Boolean = false,
    @Suppress("UNUSED_PARAMETER") onAuthSuccess: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isCheckIn = selectedTab == 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                if (isAuthFlow) "QR login" else "QR check-in / check-out",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (!isAuthFlow) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ElmsPillTab(
                        label = "Check in",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    ElmsPillTab(
                        label = "Check out",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            ElmsSectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isCheckIn) Lucide.LogIn else Lucide.LogOut,
                        contentDescription = null,
                        tint = ElmsCyan
                    )
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        if (isCheckIn) "  Scan to check in" else "  Scan to check out",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                QrScannerPlaceholder()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Point your camera at the library QR reader when prompted by staff.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            PortalButton(
                text = "Back",
                onClick = onBack,
                variant = PortalButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
