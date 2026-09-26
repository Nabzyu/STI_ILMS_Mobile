package com.example.stismartlib.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.ErrorRed
import com.example.stismartlib.ui.theme.PrimaryBlue
import com.example.stismartlib.ui.theme.SuccessGreen
import com.example.stismartlib.ui.theme.WarningYellow

enum class AppDialogType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    CONFIRM,
    LOGOUT
}

private data class DialogStyle(
    val icon: ImageVector,
    val accentColor: Color,
    val iconBackground: Color
)

private fun AppDialogType.style(isDark: Boolean): DialogStyle = when (this) {
    AppDialogType.INFO -> DialogStyle(
        icon = Icons.Outlined.Info,
        accentColor = PrimaryBlue,
        iconBackground = PrimaryBlue.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
    AppDialogType.SUCCESS -> DialogStyle(
        icon = Icons.Outlined.CheckCircle,
        accentColor = SuccessGreen,
        iconBackground = SuccessGreen.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
    AppDialogType.WARNING -> DialogStyle(
        icon = Icons.Outlined.WarningAmber,
        accentColor = WarningYellow,
        iconBackground = WarningYellow.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
    AppDialogType.ERROR -> DialogStyle(
        icon = Icons.Outlined.ErrorOutline,
        accentColor = ErrorRed,
        iconBackground = ErrorRed.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
    AppDialogType.CONFIRM -> DialogStyle(
        icon = Icons.Outlined.Info,
        accentColor = PrimaryBlue,
        iconBackground = PrimaryBlue.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
    AppDialogType.LOGOUT -> DialogStyle(
        icon = Icons.Outlined.Logout,
        accentColor = ErrorRed,
        iconBackground = ErrorRed.copy(alpha = if (isDark) 0.25f else 0.10f)
    )
}

@Composable
fun AppDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    type: AppDialogType,
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String? = null,
    onConfirm: () -> Unit = onDismiss,
    useAccentConfirm: Boolean = type == AppDialogType.SUCCESS || type == AppDialogType.CONFIRM,
    isDestructiveConfirm: Boolean = type == AppDialogType.LOGOUT || type == AppDialogType.ERROR
) {
    if (!visible) return

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val style = type.style(isDark)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(style.iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (dismissText != null) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(dismissText, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = when {
                            isDestructiveConfirm -> ButtonDefaults.buttonColors(
                                containerColor = ErrorRed,
                                contentColor = Color.White
                            )
                            useAccentConfirm -> ButtonDefaults.buttonColors(
                                containerColor = AccentYellow,
                                contentColor = PrimaryBlue
                            )
                            else -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    ) {
                        Text(confirmText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}