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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.PortalScreen
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val StudentIdPattern = Regex("^\\d{11}$")
private val CodePattern = Regex("^\\d{6}$")
private const val RESEND_DELAY_SECONDS = 60

private enum class RegisterStep { StudentId, Code, Password }

/**
 * Student sign-up via the server's enrolment-roster flow:
 * Student ID -> 6-digit code emailed to the roster's @ormoc.sti.edu.ph address -> password.
 * Name, program and year level come from the roster, so they are not collected here.
 */
@Composable
fun RegisterScreen(navController: NavHostController) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(RegisterStep.StudentId) }
    var schoolId by remember { mutableStateOf("") }
    var challengeId by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var grant by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var resendSeconds by remember { mutableStateOf(0) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var message by remember { mutableStateOf("") }
    var messageIsError by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(resendSeconds) {
        if (resendSeconds > 0) {
            delay(1_000)
            resendSeconds -= 1
        }
    }

    fun showError(text: String) {
        message = text
        messageIsError = true
    }

    fun failureMessage(error: Exception, fallback: String): String =
        if (error is ApiException && error.httpStatus == 429) {
            "Too many attempts. Please wait 15 minutes and try again."
        } else {
            userFacingMessage(error, fallback)
        }

    fun submit(action: suspend () -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        message = ""
        scope.launch {
            try {
                action()
            } catch (error: Exception) {
                showError(failureMessage(error, "Registration could not be completed. Please try again."))
            } finally {
                isSubmitting = false
            }
        }
    }

    fun requestCode() = submit {
        challengeId = repository.beginRosterRegistration(schoolId.trim())
        code = ""
        resendSeconds = RESEND_DELAY_SECONDS
        step = RegisterStep.Code
        message = "If your Student ID is on this semester's enrolment roster, a 6-digit code was sent to your " +
            "@ormoc.sti.edu.ph school email. Check your inbox and spam folder. Already registered? Sign in instead."
        messageIsError = false
    }

    fun startOver() {
        step = RegisterStep.StudentId
        challengeId = ""
        grant = ""
        code = ""
        password = ""
        confirmPassword = ""
        // Keep the cooldown: the server ignores a repeat request within a minute and the new
        // challenge ID it returns would never verify.
        errors = emptyMap()
        message = ""
    }

    PortalScreen {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        StiMark()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "CREATE ACCOUNT",
            style = com.example.stismartlib.ui.theme.EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Student registration",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (step) {
                RegisterStep.StudentId -> "Step 1 of 3 · Enter the Student ID on your school record. We send a code to " +
                    "the @ormoc.sti.edu.ph school email on the enrolment roster."
                RegisterStep.Code -> "Step 2 of 3 · Enter the 6-digit code from your school email."
                RegisterStep.Password -> "Step 3 of 3 · School email verified. Set a password to finish."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ElmsBanner(
                message = message,
                tone = if (messageIsError) ElmsBannerTone.Error else ElmsBannerTone.Info
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        when (step) {
            RegisterStep.StudentId -> PortalField(
                label = "Student ID",
                value = schoolId,
                onValueChange = { schoolId = it.filter(Char::isDigit).take(11) },
                placeholder = "Enter your Student ID",
                leadingIcon = Lucide.IdCard,
                enabled = !isSubmitting,
                error = errors["school_id"]
            )
            RegisterStep.Code -> PortalField(
                label = "Email verification code",
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(6) },
                placeholder = "6-digit code",
                leadingIcon = Lucide.Mail,
                enabled = !isSubmitting,
                error = errors["code"]
            )
            RegisterStep.Password -> {
                PortalField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "At least 8 characters",
                    leadingIcon = Lucide.LockKeyhole,
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !isSubmitting,
                    error = errors["password"],
                    trailing = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Lucide.EyeOff else Lucide.Eye,
                                contentDescription = if (showPasswords) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PortalField(
                    label = "Confirm password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Repeat your password",
                    leadingIcon = Lucide.LockKeyhole,
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    enabled = !isSubmitting,
                    error = errors["confirm_password"]
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        PortalButton(
            text = when {
                isSubmitting -> "Please wait..."
                step == RegisterStep.StudentId && resendSeconds > 0 -> "Send email code (${resendSeconds}s)"
                step == RegisterStep.StudentId -> "Send email code"
                step == RegisterStep.Code -> "Verify code"
                else -> "Create account"
            },
            variant = PortalButtonVariant.Yellow,
            enabled = !isSubmitting && !(step == RegisterStep.StudentId && resendSeconds > 0),
            loading = isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp),
            onClick = {
                when (step) {
                    RegisterStep.StudentId -> {
                        val id = schoolId.trim()
                        errors = when {
                            id.isEmpty() -> mapOf("school_id" to "Student ID is required.")
                            !StudentIdPattern.matches(id) -> mapOf("school_id" to "Student ID must be exactly 11 digits.")
                            else -> emptyMap()
                        }
                        if (errors.isEmpty()) requestCode() else showError("Please correct the highlighted fields.")
                    }
                    RegisterStep.Code -> {
                        errors = if (CodePattern.matches(code)) emptyMap() else mapOf("code" to "Enter the 6-digit code.")
                        if (errors.isNotEmpty()) {
                            showError("Please correct the highlighted fields.")
                        } else submit {
                            grant = repository.verifyRosterRegistrationCode(challengeId, code)
                            code = ""
                            step = RegisterStep.Password
                        }
                    }
                    RegisterStep.Password -> {
                        val next = mutableMapOf<String, String>()
                        if (password.length < 8) next["password"] = "Password must contain at least 8 characters."
                        else if (password.toByteArray(Charsets.UTF_8).size > 72) next["password"] = "Password is too long (72 bytes maximum)."
                        if (password != confirmPassword) next["confirm_password"] = "Password confirmation does not match."
                        errors = next
                        if (next.isNotEmpty()) {
                            showError("Please correct the highlighted fields.")
                        } else submit {
                            repository.completeRosterRegistration(challengeId, grant, password, confirmPassword)
                            password = ""
                            confirmPassword = ""
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    }
                }
            }
        )
        if (step == RegisterStep.Code) {
            Spacer(modifier = Modifier.height(12.dp))
            val canResend = resendSeconds == 0 && !isSubmitting
            Text(
                text = if (resendSeconds > 0) "Resend code in ${resendSeconds}s" else "Resend code",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (canResend) 1f else 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable(enabled = canResend) { requestCode() }
                    .padding(vertical = 12.dp)
            )
        }
        if (step != RegisterStep.StudentId) {
            Text(
                text = "Start over",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .sizeIn(minHeight = 48.dp)
                    .clickable(enabled = !isSubmitting) { startOver() }
                    .padding(vertical = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Staff accounts are set up by the library.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Already registered? Back to Login",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .clickable(enabled = !isSubmitting) { navController.popBackStack() }
                .padding(vertical = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}
