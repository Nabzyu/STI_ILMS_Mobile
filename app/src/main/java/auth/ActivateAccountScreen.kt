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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.ActivationAvailability
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Steps of account activation, mirroring the web `RosterRegistrationForm`. */
enum class ActivationStage { Checking, Closed, Unavailable, Request, Verify, Password }

/** Key the login screen reads to pre-fill the school ID after a successful activation. */
const val ActivatedSchoolIdKey = "activated_school_id"

/**
 * Student account activation against the semester enrollment roster. The code goes to the school
 * email on the roster, so students don't type an email. Staff and faculty accounts are set up by
 * the library. [previewStage] opens a step directly, for previews only.
 */
@Composable
fun ActivateAccountScreen(
    navController: NavHostController,
    previewStage: ActivationStage? = null,
) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    // Saved across rotation: the step, ID, challenge and code. The grant is memory-only.
    var stage by rememberSaveable { mutableStateOf(previewStage ?: ActivationStage.Checking) }
    var schoolId by rememberSaveable { mutableStateOf("") }
    var schoolIdError by remember { mutableStateOf<String?>(null) }
    var challengeId by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    // Short-lived grant from the code step: held by the repository in memory only (survives rotation).
    var grant by remember { mutableStateOf(repository.pendingActivationGrant.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendSecondsLeft by rememberSaveable {
        mutableIntStateOf(if (previewStage == ActivationStage.Verify) 42 else 0)
    }
    var checkAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(checkAttempt) {
        if (stage != ActivationStage.Checking) return@LaunchedEffect
        stage = try {
            when (repository.activationAvailability()) {
                ActivationAvailability.Open -> ActivationStage.Request
                ActivationAvailability.Closed -> ActivationStage.Closed
                ActivationAvailability.Unavailable -> ActivationStage.Unavailable
            }
        } catch (_: Exception) {
            ActivationStage.Unavailable
        }
    }
    LaunchedEffect(stage, resendSecondsLeft) {
        if (stage == ActivationStage.Verify && resendSecondsLeft > 0) {
            delay(1_000)
            resendSecondsLeft -= 1
        }
    }

    fun startOver(error: String? = null) {
        repository.clearPendingRecovery()
        stage = ActivationStage.Request
        challengeId = ""
        code = ""
        codeError = null
        grant = ""
        password = ""
        confirmation = ""
        passwordError = null
        notice = null
        banner = error
    }

    fun backToSignIn(activatedSchoolId: String? = null) {
        repository.clearPendingRecovery()
        grant = ""
        password = ""
        confirmation = ""
        if (activatedSchoolId != null) {
            navController.previousBackStackEntry?.savedStateHandle?.set(ActivatedSchoolIdKey, activatedSchoolId)
        }
        navController.popBackStack()
    }

    // The grant isn't saved to disk, so if the app was closed in the background the password step
    // can't finish; start again with a clear message instead.
    LaunchedEffect(Unit) {
        if (stage == ActivationStage.Password && grant.isBlank() && previewStage != ActivationStage.Password) {
            startOver("Your activation step timed out. Please request a new code.")
        }
    }

    BackHandler(
        enabled = !isBusy && !isResending &&
            (stage == ActivationStage.Verify || stage == ActivationStage.Password),
    ) { startOver() }

    fun sendCode(onSent: () -> Unit) {
        scope.launch {
            try {
                challengeId = repository.beginActivation(schoolId)
                code = ""
                codeError = null
                resendSecondsLeft = 60
                stage = ActivationStage.Verify
                notice = "If your Student ID is on this semester's roster, a code was sent to your school email. Check your inbox and spam folder. If you already have an account, sign in instead."
                onSent()
            } catch (error: ApiException) {
                val message = userFacingMessage(error, "The code could not be sent. Please try again.")
                when (error.code) {
                    "STUDENT_ID_INVALID" -> schoolIdError = message
                    "ROSTER_REGISTRATION_CLOSED" -> stage = ActivationStage.Closed
                    else -> banner = message
                }
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
            text = "NEW STUDENT",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (stage) {
                ActivationStage.Verify -> "Check your email"
                ActivationStage.Password -> "Set your password"
                else -> "Activate account"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (stage) {
                ActivationStage.Checking -> "Checking whether account activation is open…"
                ActivationStage.Closed ->
                    "Account activation opens when the library starts the semester. If you already have an account, sign in instead."
                ActivationStage.Unavailable -> "Account activation is temporarily unavailable."
                ActivationStage.Request ->
                    "Enter the Student ID on your school record. We'll send a code to the school email on the enrollment roster. Faculty and staff accounts are set up by the library."
                ActivationStage.Verify ->
                    "Enter the code from your school email. It expires in 10 minutes."
                ActivationStage.Password ->
                    "School email verified. Set a password to finish activating your account."
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
            ElmsBanner(message = notice.orEmpty(), tone = ElmsBannerTone.Info)
        }
        Spacer(modifier = Modifier.height(28.dp))

        when (stage) {
            ActivationStage.Checking -> ElmsLoadingBlock()

            ActivationStage.Closed -> PortalButton(
                text = "Back to sign in",
                onClick = { backToSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 52.dp)
            )

            ActivationStage.Unavailable -> {
                PortalButton(
                    text = "Try again",
                    onClick = {
                        stage = ActivationStage.Checking
                        checkAttempt += 1
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 52.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PortalButton(
                    text = "Back to sign in",
                    onClick = { backToSignIn() },
                    variant = PortalButtonVariant.Ghost,
                    leadingIcon = Lucide.ArrowLeft,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            ActivationStage.Request -> {
                PortalField(
                    label = "Student ID",
                    value = schoolId,
                    onValueChange = { value ->
                        schoolId = value.filter { it.isLetterOrDigit() || it in "._-" }.take(50)
                        schoolIdError = null
                    },
                    placeholder = "Enter your Student ID",
                    leadingIcon = Lucide.IdCard,
                    error = schoolIdError,
                    enabled = !isBusy,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PortalButton(
                    text = if (isBusy) "Sending..." else "Send email code",
                    onClick = {
                        if (isBusy) return@PortalButton
                        schoolIdError = when {
                            normalizeSchoolId(schoolId).isEmpty() -> "Student ID is required."
                            loginSchoolIdError(schoolId) != null -> "Enter a valid Student ID."
                            else -> null
                        }
                        if (schoolIdError != null) return@PortalButton
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

            ActivationStage.Verify -> LoginCodeStep(
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
                            grant = repository.verifyActivationCode(challengeId, code)
                            code = ""
                            notice = null
                            stage = ActivationStage.Password
                        } catch (error: ApiException) {
                            code = ""
                            val message = userFacingMessage(error, "The code could not be verified.")
                            if (error.code == "ROSTER_REGISTRATION_CLOSED") stage = ActivationStage.Closed
                            else codeError = message
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
                    sendCode { notice = "A new code was sent if your Student ID is eligible. Use the newest code." }
                },
                onBack = { startOver() },
                verifyLabel = "Verify email code",
                backLabel = "Start over",
                codeLabel = "Email verification code",
            )

            ActivationStage.Password -> {
                val transformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                PortalField(
                    label = "Password",
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    placeholder = "At least 8 characters",
                    leadingIcon = Lucide.LockKeyhole,
                    visualTransformation = transformation,
                    trailing = {
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
                    },
                    enabled = !isBusy,
                )
                Spacer(modifier = Modifier.height(20.dp))
                PortalField(
                    label = "Confirm password",
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
                    text = if (isBusy) "Activating..." else "Activate account",
                    onClick = {
                        if (isBusy) return@PortalButton
                        passwordError = newPasswordError(password, confirmation)
                        if (passwordError != null) return@PortalButton
                        isBusy = true
                        banner = null
                        scope.launch {
                            try {
                                val activated = repository.completeActivation(challengeId, grant, password, confirmation)
                                backToSignIn(activated ?: normalizeSchoolId(schoolId))
                            } catch (error: ApiException) {
                                val message = userFacingMessage(error, "The account could not be activated.")
                                when (error.code) {
                                    "ROSTER_CODE_INVALID" -> startOver(message)
                                    "ROSTER_REGISTRATION_CLOSED" -> stage = ActivationStage.Closed
                                    "ROSTER_PASSWORD_INVALID" -> passwordError = message
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
                    text = "Start over",
                    onClick = { startOver() },
                    variant = PortalButtonVariant.Ghost,
                    enabled = !isBusy,
                    leadingIcon = Lucide.ArrowLeft,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    }
}
