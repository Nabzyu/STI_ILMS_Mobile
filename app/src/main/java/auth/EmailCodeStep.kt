package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import kotlinx.coroutines.delay

/** Six-digit code entry shared by login and registration email verification. */
@Composable
fun EmailCodeStep(
    instructions: String,
    code: String,
    onCodeChange: (String) -> Unit,
    error: String?,
    isSubmitting: Boolean,
    isResending: Boolean,
    /** Bump to restart the resend cooldown (e.g. after a successful resend). */
    cooldownKey: Int,
    cooldownSeconds: Int,
    verifyLabel: String,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    backLabel: String,
    onBack: () -> Unit,
) {
    var secondsLeft by remember { mutableIntStateOf(cooldownSeconds) }
    LaunchedEffect(cooldownKey) {
        secondsLeft = cooldownSeconds
        while (secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
        }
    }

    Column {
        Text(
            text = instructions,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        PortalField(
            label = "Verification code",
            value = code,
            onValueChange = { onCodeChange(it.filter(Char::isDigit).take(6)) },
            placeholder = "6-digit code",
            leadingIcon = Lucide.Mail,
            error = error,
        )
        Spacer(modifier = Modifier.height(20.dp))
        PortalButton(
            text = if (isSubmitting) "Verifying..." else verifyLabel,
            onClick = onVerify,
            enabled = !isSubmitting && code.length == 6,
            loading = isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        PortalButton(
            text = when {
                isResending -> "Sending..."
                secondsLeft > 0 -> "Resend code in ${secondsLeft}s"
                else -> "Resend code"
            },
            onClick = onResend,
            variant = PortalButtonVariant.Secondary,
            enabled = !isResending && !isSubmitting && secondsLeft == 0,
            loading = isResending,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = backLabel,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .clickable(enabled = !isSubmitting, onClick = onBack)
                .padding(vertical = 12.dp),
        )
    }
}
