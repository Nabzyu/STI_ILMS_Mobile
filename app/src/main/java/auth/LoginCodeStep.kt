package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField

/**
 * Second sign-in step for Student and Faculty accounts: the six-digit code emailed by the server.
 * Stateless; [LoginScreen] owns the challenge, the code and the resend countdown.
 */
@Composable
fun LoginCodeStep(
    code: String,
    onCodeChange: (String) -> Unit,
    codeError: String?,
    isVerifying: Boolean,
    isResending: Boolean,
    resendSecondsLeft: Int,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val busy = isVerifying || isResending
    Column(modifier = modifier.fillMaxWidth()) {
        PortalField(
            label = "Login code",
            value = code,
            onValueChange = { onCodeChange(it.filter(Char::isDigit).take(6)) },
            placeholder = "Enter the 6-digit code",
            leadingIcon = Lucide.Mail,
            error = codeError,
            enabled = !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        Spacer(modifier = Modifier.height(20.dp))
        PortalButton(
            text = if (isVerifying) "Verifying..." else "Verify and sign in",
            onClick = onVerify,
            enabled = !busy && isValidLoginCode(code),
            loading = isVerifying,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        PortalButton(
            text = when {
                isResending -> "Sending..."
                resendSecondsLeft > 0 -> "Resend code in ${resendSecondsLeft}s"
                else -> "Resend code"
            },
            onClick = onResend,
            variant = PortalButtonVariant.Secondary,
            enabled = !busy && resendSecondsLeft <= 0,
            loading = isResending,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        PortalButton(
            text = "Back to sign in",
            onClick = onBack,
            variant = PortalButtonVariant.Ghost,
            enabled = !busy,
            leadingIcon = Lucide.ArrowLeft,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
