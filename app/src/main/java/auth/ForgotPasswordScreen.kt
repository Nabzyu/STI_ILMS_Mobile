package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.launch

/** Which part of recovery is showing; the preview params start mid-flow. */
enum class RecoveryStep { Identify, Code, NewPassword, Done }

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    initialStep: RecoveryStep = RecoveryStep.Identify,
    initialNotice: String? = null,
) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(initialStep) }
    var identifier by remember { mutableStateOf("") }
    var challengeId by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var fieldError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    // Server copy shown above the current step (neutral notice, errors, or the done message).
    var notice by remember { mutableStateOf(initialNotice) }
    var isError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var cooldownKey by remember { mutableIntStateOf(0) }

    fun backToLogin() {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    fun submit(action: suspend () -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        scope.launch {
            try {
                action()
            } catch (error: Exception) {
                isError = true
                notice = userFacingMessage(error, "Something went wrong. Please try again.")
            } finally {
                isSubmitting = false
            }
        }
    }

    PortalScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            StiMark()
            Spacer(modifier = Modifier.height(32.dp))
            Text("ACCOUNT RECOVERY", style = EyebrowStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Reset password",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            notice?.let {
                Spacer(modifier = Modifier.height(16.dp))
                ElmsBanner(
                    message = it,
                    tone = when {
                        isError -> ElmsBannerTone.Error
                        step == RecoveryStep.Done -> ElmsBannerTone.Success
                        else -> ElmsBannerTone.Info
                    },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            when (step) {
                RecoveryStep.Identify -> {
                    PortalField(
                        label = "School ID or school email",
                        value = identifier,
                        onValueChange = { identifier = it; fieldError = null },
                        placeholder = "02000123456 or name@ormoc.sti.edu.ph",
                        leadingIcon = Lucide.IdCard,
                        error = fieldError,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PortalButton(
                        text = if (isSubmitting) "Sending code..." else "Send code",
                        enabled = !isSubmitting,
                        loading = isSubmitting,
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 52.dp),
                        onClick = {
                            if (identifier.isBlank()) {
                                fieldError = "Enter your School ID or school email."
                                return@PortalButton
                            }
                            submit {
                                val started = repository.startPasswordRecovery(identifier)
                                challengeId = started.challengeId.orEmpty()
                                isError = false
                                notice = started.message
                                code = ""
                                cooldownKey++
                                step = RecoveryStep.Code
                            }
                        },
                    )
                }
                RecoveryStep.Code -> EmailCodeStep(
                    instructions = "Enter the 6-digit code from your school email.",
                    code = code,
                    onCodeChange = { code = it },
                    error = null,
                    isSubmitting = isSubmitting,
                    isResending = isResending,
                    cooldownKey = cooldownKey,
                    cooldownSeconds = 60,
                    verifyLabel = "Verify code",
                    onVerify = {
                        submit {
                            resetToken = repository.verifyRecoveryCode(challengeId, code)
                            isError = false
                            notice = null
                            step = RecoveryStep.NewPassword
                        }
                    },
                    onResend = {
                        isResending = true
                        scope.launch {
                            try {
                                // A new request replaces the challenge; older codes stop working.
                                val started = repository.startPasswordRecovery(identifier)
                                challengeId = started.challengeId.orEmpty()
                                code = ""
                                cooldownKey++
                                isError = false
                                notice = started.message
                            } catch (error: Exception) {
                                isError = true
                                notice = userFacingMessage(error, "The code could not be sent.")
                            } finally {
                                isResending = false
                            }
                        }
                    },
                    backLabel = "Back to Login",
                    onBack = ::backToLogin,
                )
                RecoveryStep.NewPassword -> {
                    PortalField(
                        label = "New password",
                        value = password,
                        onValueChange = { password = it; fieldError = null },
                        placeholder = "At least 8 characters",
                        leadingIcon = Lucide.LockKeyhole,
                        visualTransformation = PasswordVisualTransformation(),
                        error = fieldError,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    PortalField(
                        label = "Confirm new password",
                        value = confirm,
                        onValueChange = { confirm = it; confirmError = null },
                        placeholder = "Repeat your new password",
                        leadingIcon = Lucide.LockKeyhole,
                        visualTransformation = PasswordVisualTransformation(),
                        error = confirmError,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PortalButton(
                        text = if (isSubmitting) "Saving..." else "Save new password",
                        enabled = !isSubmitting,
                        loading = isSubmitting,
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 52.dp),
                        onClick = {
                            fieldError = if (password.length < 8) "Password must contain at least 8 characters." else null
                            confirmError = if (password != confirm) "Password confirmation does not match." else null
                            if (fieldError != null || confirmError != null) return@PortalButton
                            submit {
                                val message = repository.resetPassword(challengeId, resetToken, password, confirm)
                                password = ""
                                confirm = ""
                                isError = false
                                notice = message
                                step = RecoveryStep.Done
                            }
                        },
                    )
                }
                RecoveryStep.Done -> PortalButton(
                    text = "Back to Login",
                    onClick = ::backToLogin,
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 52.dp),
                )
            }
            if (step == RecoveryStep.Identify || step == RecoveryStep.NewPassword) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Back to Login",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .sizeIn(minHeight = 48.dp)
                        .clickable(enabled = !isSubmitting, onClick = ::backToLogin)
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}
