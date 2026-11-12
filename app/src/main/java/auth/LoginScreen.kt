package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.LoginChallenge
import com.example.stismartlib.data.LoginOutcome
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.sessionEndedMessage
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Sign-in for every role. Student/Faculty use the `user` portal and then enter an emailed code;
 * Librarians switch to the librarian sign-in (the server's separate `staff` portal).
 * [previewChallenge] opens the code step directly, for previews only.
 */
@Composable
fun LoginScreen(navController: NavHostController, previewChallenge: LoginChallenge? = null) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var schoolId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var schoolIdError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var staffPortal by remember { mutableStateOf(false) }
    var portalChosenByUser by remember { mutableStateOf(false) }
    var challenge by remember { mutableStateOf(previewChallenge) }
    var code by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var isResending by remember { mutableStateOf(false) }
    var resendSecondsLeft by remember { mutableIntStateOf(previewChallenge?.resendAfterSeconds ?: 0) }

    LaunchedEffect(Unit) {
        repository.consumeSessionEndReason()?.let { notice = sessionEndedMessage(it) }
    }
    // Pre-select the portal this school ID last signed in with, unless the user picked one.
    LaunchedEffect(schoolId) {
        if (!portalChosenByUser) repository.prefersStaffPortal(schoolId)?.let { staffPortal = it }
    }
    LaunchedEffect(challenge, resendSecondsLeft) {
        if (challenge != null && resendSecondsLeft > 0) {
            delay(1_000)
            resendSecondsLeft -= 1
        }
    }

    fun goToWorkspace() {
        password = ""
        val destination = if (repository.session.userType.isStaff) {
            Screen.StaffHome.route
        } else {
            Screen.Dashboard.create(repository.session.userType)
        }
        navController.navigate(destination) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    fun leaveCodeStep() {
        challenge = null
        code = ""
        codeError = null
        banner = null
        notice = null
    }

    BackHandler(enabled = challenge != null && !isSubmitting && !isResending) { leaveCodeStep() }

    val activeChallenge = challenge

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
            text = when {
                activeChallenge != null -> "CHECK YOUR EMAIL"
                staffPortal -> "LIBRARIAN"
                else -> "WELCOME BACK"
            },
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                activeChallenge != null -> "Enter your code"
                staffPortal -> "Librarian sign in"
                else -> "Sign in"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                activeChallenge != null ->
                    "We sent a six-digit code to ${activeChallenge.emailHint}. " +
                        "It expires in ${(activeChallenge.expiresInSeconds / 60).coerceAtLeast(1)} minutes."
                staffPortal ->
                    "For librarian accounts. Use your school ID and password."
                else ->
                    "Use your school ID and password. Students and faculty then enter a code sent to their school email."
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
        if (activeChallenge != null) {
            LoginCodeStep(
                code = code,
                onCodeChange = {
                    code = it
                    codeError = null
                },
                codeError = codeError,
                isVerifying = isSubmitting,
                isResending = isResending,
                resendSecondsLeft = resendSecondsLeft,
                onVerify = {
                    if (isSubmitting || !isValidLoginCode(code)) return@LoginCodeStep
                    isSubmitting = true
                    banner = null
                    notice = null
                    scope.launch {
                        try {
                            repository.verifyLoginCode(schoolId, activeChallenge.challengeId, code)
                            goToWorkspace()
                        } catch (error: ApiException) {
                            code = ""
                            val message = userFacingMessage(error, "The code could not be verified.")
                            if (error.code == "LOGIN_CODE_INVALID") codeError = message else banner = message
                            // A changed account or a paused challenge needs a fresh sign-in.
                            if (error.code == "LOGIN_CHANGED" || error.code == "LOGIN_CODE_PAUSED") {
                                challenge = null
                            }
                        } catch (error: Exception) {
                            banner = userFacingMessage(error)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                onResend = {
                    if (isResending || resendSecondsLeft > 0) return@LoginCodeStep
                    isResending = true
                    banner = null
                    notice = null
                    scope.launch {
                        try {
                            resendSecondsLeft = repository.resendLoginCode(activeChallenge.challengeId)
                            code = ""
                            notice = "A new code was sent. Use the newest code."
                        } catch (error: ApiException) {
                            banner = userFacingMessage(error, "The code could not be sent.")
                        } catch (error: Exception) {
                            banner = userFacingMessage(error)
                        } finally {
                            isResending = false
                        }
                    }
                },
                onBack = { leaveCodeStep() },
            )
        } else {
        PortalField(
            label = "School ID",
            value = schoolId,
            onValueChange = { value ->
                schoolId = value.filter { it.isLetterOrDigit() || it in "._-" }.take(50)
            },
            placeholder = if (staffPortal) "Enter your librarian school ID" else "Enter your Student or Faculty ID",
            leadingIcon = Lucide.IdCard,
            error = schoolIdError
        )
        Spacer(modifier = Modifier.height(20.dp))
        PortalField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter your password",
            leadingIcon = Lucide.LockKeyhole,
            visualTransformation = if (showPassword) {
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            error = passwordError,
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
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .clickable(
                    onClick = { showPassword = !showPassword },
                    role = androidx.compose.ui.semantics.Role.Checkbox
                )
        ) {
            Checkbox(
                checked = showPassword,
                onCheckedChange = { showPassword = it },
                colors = CheckboxDefaults.colors(checkedColor = ElmsCyan, uncheckedColor = ElmsCyan)
            )
            Text(
                "Show password",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PortalButton(
            text = if (isSubmitting) "Signing in..." else "Sign in",
            onClick = {
                if (isSubmitting) return@PortalButton
                val normalized = normalizeSchoolId(schoolId)
                schoolIdError = loginSchoolIdError(normalized)
                passwordError = if (password.isEmpty()) "Password is required." else null
                if (schoolIdError != null || passwordError != null) {
                    banner = null
                    return@PortalButton
                }
                isSubmitting = true
                banner = null
                notice = null
                scope.launch {
                    try {
                        when (val outcome = repository.login(normalized, password, staffPortal)) {
                            is LoginOutcome.SignedIn -> goToWorkspace()
                            is LoginOutcome.CodeRequired -> {
                                password = ""
                                code = ""
                                codeError = null
                                schoolId = normalized
                                resendSecondsLeft = outcome.challenge.resendAfterSeconds
                                challenge = outcome.challenge
                            }
                        }
                    } catch (error: ApiException) {
                        banner = userFacingMessage(error, "Invalid school ID or password.")
                        schoolIdError = error.fieldErrors["school_id"]
                        passwordError = error.fieldErrors["password"]
                    } catch (error: Exception) {
                        banner = userFacingMessage(error)
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            enabled = !isSubmitting,
            loading = isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = buildAnnotatedString {
                    append(if (staffPortal) "Student or faculty? " else "Librarian? ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            textDecoration = TextDecoration.Underline,
                            color = ElmsCyan
                        )
                    ) {
                        append(if (staffPortal) "Use student sign in" else "Use librarian sign in")
                    }
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable(enabled = !isSubmitting) {
                        staffPortal = !staffPortal
                        portalChosenByUser = true
                        banner = null
                        schoolIdError = null
                        passwordError = null
                    }
                    .padding(vertical = 12.dp)
            )
        }
        if (!staffPortal) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            textDecoration = TextDecoration.Underline,
                            color = ElmsCyan
                        )
                    ) {
                        append("Register as Student")
                    }
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable { navController.navigate(Screen.Register.route) }
                    .padding(vertical = 12.dp)
            )
        }
        }
        }
    }
    }
}
