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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

private val StudentIdPattern = Regex("^\\d{11}$")
private val StiEmailPattern = Regex("^[A-Za-z0-9._%+-]+@stu\\.sti\\.edu\\.ph$", RegexOption.IGNORE_CASE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var schoolId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var stiEmail by remember { mutableStateOf("") }
    var selectedProgram by remember { mutableStateOf("") }
    var selectedYearLevel by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var programMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val yearLevelOptions = yearLevelsForProgram(selectedProgram)

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
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ElmsBanner(
                message = message,
                tone = if (message.contains("correct", ignoreCase = true)) {
                    ElmsBannerTone.Error
                } else {
                    ElmsBannerTone.Info
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        PortalField(
            label = "Student ID",
            value = schoolId,
            onValueChange = { schoolId = it.filter(Char::isDigit).take(11) },
            placeholder = "Enter your Student ID",
            leadingIcon = Lucide.IdCard,
            error = errors["school_id"]
        )
        Spacer(modifier = Modifier.height(14.dp))
        PortalField(
            label = "First name",
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = "First name",
            leadingIcon = Lucide.UserRound,
            error = errors["first_name"]
        )
        Spacer(modifier = Modifier.height(14.dp))
        PortalField(
            label = "Last name",
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = "Last name",
            leadingIcon = Lucide.UserRound,
            error = errors["last_name"]
        )
        Spacer(modifier = Modifier.height(14.dp))
        PortalField(
            label = "STI Email Address",
            value = stiEmail,
            onValueChange = { stiEmail = it },
            placeholder = "name@stu.sti.edu.ph",
            leadingIcon = Lucide.Mail,
            error = errors["email"]
        )
        Spacer(modifier = Modifier.height(14.dp))
        val programSelected = selectedProgram.isNotEmpty()
        ExposedDropdownMenuBox(expanded = programMenuOpen, onExpandedChange = { programMenuOpen = it }) {
            PortalField(
                label = "Program or strand",
                value = selectedProgram,
                onValueChange = {},
                placeholder = "Select a program",
                leadingIcon = Lucide.GraduationCap,
                trailingIcon = Lucide.ChevronDown,
                readOnly = true,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                error = errors["program_strand"]
            )
            ExposedDropdownMenu(expanded = programMenuOpen, onDismissRequest = { programMenuOpen = false }) {
                ProgramOption.allDisplayNames.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedProgram = option
                            selectedYearLevel = ""
                            programMenuOpen = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        ExposedDropdownMenuBox(
            expanded = yearMenuOpen,
            onExpandedChange = { if (programSelected) yearMenuOpen = it }
        ) {
            PortalField(
                label = "Year or grade level",
                value = selectedYearLevel,
                onValueChange = {},
                placeholder = if (programSelected) "Select a year level" else "Select a program first",
                leadingIcon = Lucide.Calendar,
                trailingIcon = Lucide.ChevronDown,
                readOnly = true,
                enabled = programSelected,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = programSelected),
                error = errors["year_grade_level"]
            )
            ExposedDropdownMenu(expanded = yearMenuOpen, onDismissRequest = { yearMenuOpen = false }) {
                yearLevelOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedYearLevel = option
                            yearMenuOpen = false
                        }
                    )
                }
            }
        }
        if (!programSelected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select a program first to choose a year or grade level.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        PortalField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "At least 8 characters",
            leadingIcon = Lucide.LockKeyhole,
            visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
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
            error = errors["confirm_password"]
        )
        Spacer(modifier = Modifier.height(20.dp))
        PortalButton(
            text = if (isSubmitting) "Creating account..." else "Create account",
            variant = PortalButtonVariant.Yellow,
            enabled = !isSubmitting,
            loading = isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp),
            onClick = {
                if (isSubmitting) return@PortalButton
                val next = mutableMapOf<String, String>()
                val normalized = schoolId.trim()
                val normalizedEmail = stiEmail.trim().lowercase()
                if (normalized.isEmpty()) next["school_id"] = "Student ID is required."
                else if (!StudentIdPattern.matches(normalized)) next["school_id"] = "Student ID must be exactly 11 digits."
                if (firstName.isBlank()) next["first_name"] = "First name is required."
                if (lastName.isBlank()) next["last_name"] = "Last name is required."
                if (normalizedEmail.isEmpty()) next["email"] = "STI Email Address is required."
                else if (!StiEmailPattern.matches(normalizedEmail)) {
                    next["email"] = "Enter a valid STI email (name@stu.sti.edu.ph)."
                }
                if (selectedProgram.isBlank()) next["program_strand"] = "Program or strand is required."
                if (selectedYearLevel.isBlank()) next["year_grade_level"] = "Year or grade level is required."
                if (password.length < 8) next["password"] = "Password must contain at least 8 characters."
                if (password != confirmPassword) next["confirm_password"] = "Password confirmation does not match."
                errors = next
                if (next.isNotEmpty()) {
                    message = "Please correct the highlighted fields."
                    return@PortalButton
                }
                isSubmitting = true
                message = ""
                scope.launch {
                    try {
                        repository.registerStudent(
                            schoolId = normalized,
                            firstName = firstName,
                            lastName = lastName,
                            email = normalizedEmail,
                            programStrand = selectedProgram,
                            yearGradeLevel = selectedYearLevel,
                            password = password,
                            confirmPassword = confirmPassword,
                        )
                        password = ""
                        confirmPassword = ""
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    } catch (error: ApiException) {
                        errors = error.fieldErrors
                        message = userFacingMessage(error, "Registration could not be completed. Check your details and try again.")
                    } catch (error: Exception) {
                        message = userFacingMessage(error)
                    } finally {
                        isSubmitting = false
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Already registered? Back to Login",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .clickable { navController.popBackStack() }
                .padding(vertical = 12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}
