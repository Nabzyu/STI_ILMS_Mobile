package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Steps of password recovery, mirroring the web `PasswordRecoveryPage`. */
enum class PasswordResetStage { Request, Verify, Reset, Done }

/**
 * Password recovery for Student accounts, or library Staff accounts with [staff].
 * The server only sends codes to those roles; faculty and librarians are told to contact the library.
 * [previewStage] opens a later step directly, for previews only.
 */
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    staff: Boolean,
    previewStage: PasswordResetStage = PasswordResetStage.Request,
) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var stage by remember { mutableStateOf(previewStage) }
    var identifier by remember { mutableStateOf("") }
    var identifierError by remember { mutableStateOf<String?>(null) }
    var challengeId by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    // Short-lived reset token: kept in memory only, never stored.
    var resetToken by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendSecondsLeft by remember {
        mutableIntStateOf(if (previewStage == PasswordResetStage.Verify) 42 else 0)
    }

    LaunchedEffect(stage, resendSecondsLeft) {
        if (stage == PasswordResetStage.Verify && resendSecondsLeft > 0) {
            delay(1_000)
            resendSecondsLeft -= 1
        }
    }

    fun startAgain(error: String? = null) {
        stage = PasswordResetStage.Request
        challengeId = ""
        code = ""
        codeError = null
        resetToken = ""
        password = ""
        confirmation = ""
        passwordError = null
        notice = null
        banner = error
    }

    fun backToSignIn() {
        resetToken = ""
        password = ""
        confirmation = ""
        navController.popBackStack()
    }

    // From the code or new-password step, back starts recovery again instead of leaving.
    BackHandler(
        enabled = !isBusy && !isResending &&
            (stage == PasswordResetStage.Verify || stage == PasswordResetStage.Reset),
    ) { startAgain() }

    fun sendCode(onSent: () -> Unit) {
        scope.launch {
            try {
                val challenge = repository.requestPasswordReset(identifier, staff)
                challengeId = challenge.challengeId
                code = ""
                codeError = null
                resendSecondsLeft = 60
                stage = PasswordResetStage.Verify
                notice = challenge.message ?: "If an account matches, a six-digit code was sent to its school email."
                onSent()
            } catch (error: ApiException) {
                banner = userFacingMessage(error, "The code could not be sent. Please try again.")
                identifierError = error.fieldErrors["identifier"]
            } catch (error: Exception) {
                banner = userFacingMessage(error)
            } finally {
                isBusy = false
                isResending = false
            }
        }
    }

    PortalScreen {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        StiMark()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (staff) "LIBRARY STAFF" else "PASSWORD RESET",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (stage) {
                PasswordResetStage.Request -> "Forgot password"
                PasswordResetStage.Verify -> "Check your email"
                PasswordResetStage.Reset -> "Choose a new password"
                PasswordResetStage.Done -> "Password updated"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (stage) {
                PasswordResetStage.Request -> if (staff) {
                    "For library staff accounts set up by the library. We'll email a six-digit code to the school email on your account. Librarian passwords are reset by the library administrator."
                } else {
                    "For student accounts. We'll email a six-digit code to the school email on your account. Faculty and librarians: please contact the library to reset your password."
                }
                PasswordResetStage.Verify ->
                    "Enter the code from your school email. It expires in 10 minutes. Three incorrect codes lock password reset for 15 minutes."
                PasswordResetStage.Reset ->
                    "Choose a new password within 5 minutes. You'll be signed out on all other devices."
                PasswordResetStage.Done ->
                    "Sign in with your new password."
            },
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        if (banner != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ElmsBanner(message = banner.orEmpty(), tone = ElmsBannerTone.Error)
        }
        if (notice != null) {
            Spacer(modifier = Modifier.height(if (banner != null) 12.dp else 24.dp))
            ElmsBanner(
                message = notice.orEmpty(),
                tone = if (stage == PasswordResetStage.Done) ElmsBannerTone.Success else ElmsBannerTone.Info
            )
        }
        Spacer(modifier = Modifier.height(28.dp))

        when (stage) {
            PasswordResetStage.Request -> {
                PortalField(
                    label = if (staff) "Staff school ID or school email" else "Student ID or school email",
                    value = identifier,
                    onValueChange = {
                        identifier = it.take(254)
                        identifierError = null
                    },
                    placeholder = "Enter your school ID or email",
                    leadingIcon = Lucide.IdCard,
                    error = identifierError,
                    enabled = !isBusy,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PortalButton(
                    text = if (isBusy) "Sending..." else "Send code",
                    onClick = {
                        if (isBusy) return@PortalButton
                        if (identifier.isBlank()) {
                            identifierError = "Enter your school ID or school email."
                            return@PortalButton
                        }
                        isBusy = true
                        banner = null
                        notice = null
                        sendCode {}
                    },
                    enabled = !isBusy,
                    loading = isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 52.dp)
                )
            }

            PasswordResetStage.Verify -> LoginCodeStep(
                code = code,
                onCodeChange = {
                    code = it
                    codeError = null
                },
                codeError = codeError,
                isVerifying = isBusy,
                isResending = isResending,
                resendSecondsLeft = resendSecondsLeft,
                onVerify = {
                    if (isBusy || !isValidLoginCode(code)) return@LoginCodeStep
                    isBusy = true
                    banner = null
                    scope.launch {
                        try {
                            resetToken = repository.verifyPasswordResetCode(challengeId, code)
                            code = ""
                            notice = null
                            stage = PasswordResetStage.Reset
                        } catch (error: ApiException) {
                            code = ""
                            val message = userFacingMessage(error, "The code could not be verified.")
                            if (error.code == "RECOVERY_LOCKED") startAgain(message) else codeError = message
                        } catch (error: Exception) {
                            banner = userFacingMessage(error)
                        } finally {
                            isBusy = false
                        }
                    }
                },
                onResend = {
                    if (isResending || resendSecondsLeft > 0) return@LoginCodeStep
                    isResending = true
                    banner = null
                    // A resend is a new request: the server issues a new challenge and code.
                    sendCode { notice = "A new code was sent if an account matches. Use the newest code." }
                },
                onBack = { startAgain() },
                verifyLabel = "Verify code",
                backLabel = "Start again",
                codeLabel = "Verification code",
            )

            PasswordResetStage.Reset -> {
                val transformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                val toggle: @Composable () -> Unit = {
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Lucide.EyeOff else Lucide.Eye,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                PortalField(
                    label = "New password",
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    placeholder = "At least 8 characters",
                    leadingIcon = Lucide.LockKeyhole,
                    visualTransformation = transformation,
                    trailing = toggle,
                    enabled = !isBusy,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PortalField(
                    label = "Confirm new password",
                    value = confirmation,
                    onValueChange = {
                        confirmation = it
                        passwordError = null
                    },
                    placeholder = "Enter it again",
                    leadingIcon = Lucide.LockKeyhole,
                    visualTransformation = transformation,
                    error = passwordError,
                    enabled = !isBusy,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PortalButton(
                    text = if (isBusy) "Updating..." else "Update password",
                    onClick = {
                        if (isBusy) return@PortalButton
                        passwordError = newPasswordError(password, confirmation)
                        if (passwordError != null) return@PortalButton
                        isBusy = true
                        banner = null
                        scope.launch {
                            try {
                                repository.resetPassword(challengeId, resetToken, password, confirmation)
                                resetToken = ""
                                password = ""
                                confirmation = ""
                                notice = "Your password was changed."
                                stage = PasswordResetStage.Done
                            } catch (error: ApiException) {
                                val message = userFacingMessage(error, "The password could not be updated.")
                                when (error.code) {
                                    "RECOVERY_TOKEN_INVALID" -> startAgain(message)
                                    "PASSWORD_UNCHANGED", "AUTH_VALIDATION_FAILED" -> passwordError = message
                                    else -> banner = message
                                }
                            } catch (error: Exception) {
                                banner = userFacingMessage(error)
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    loading = isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 52.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PortalButton(
                    text = "Start again",
                    onClick = { startAgain() },
                    variant = PortalButtonVariant.Ghost,
                    enabled = !isBusy,
                    leadingIcon = Lucide.ArrowLeft,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PasswordResetStage.Done -> PortalButton(
                text = "Back to sign in",
                onClick = { backToSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 52.dp)
            )
        }

        if (stage == PasswordResetStage.Request) {
            Spacer(modifier = Modifier.height(8.dp))
            PortalButton(
                text = "Back to sign in",
                onClick = { backToSignIn() },
                variant = PortalButtonVariant.Ghost,
                enabled = !isBusy,
                leadingIcon = Lucide.ArrowLeft,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    }
}
