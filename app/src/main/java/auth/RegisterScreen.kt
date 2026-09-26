package com.example.stismartlib.ui.auth
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.ui.components.LibraryMark
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.theme.PrimaryBlue

private val SchoolIdPattern = Regex("^[A-Z0-9][A-Z0-9._-]{2,49}$")
private val ContactPattern = Regex("^\\+?[0-9 ()-]{7,30}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    isFaculty: Boolean = false
) {
    var schoolId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var selectedProgram by remember { mutableStateOf("") }
    var selectedYearLevel by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var programMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var message by remember { mutableStateOf("") }

    val yearLevelOptions = yearLevelsForProgram(selectedProgram)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        LibraryMark()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "CREATE ACCOUNT",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.6.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Student registration", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Black)
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(20.dp))
        PortalField(
            label = "Student ID",
            value = schoolId,
            onValueChange = { schoolId = it.uppercase() },
            placeholder = "Enter your STI Student ID",
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
            label = "Contact number",
            value = contactNumber,
            onValueChange = { contactNumber = it },
            placeholder = "09xx xxx xxxx",
            leadingIcon = Lucide.Phone,
            error = errors["contact_number"]
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text("Program or strand", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = programMenuOpen, onExpandedChange = { programMenuOpen = it }) {
            PortalField(
                label = "",
                value = selectedProgram,
                onValueChange = {},
                placeholder = "Select a program",
                leadingIcon = Lucide.UserRound,
                modifier = Modifier.menuAnchor(),
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
        Text("Year or grade level", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = yearMenuOpen, onExpandedChange = { if (selectedProgram.isNotEmpty()) yearMenuOpen = it }) {
            PortalField(
                label = "",
                value = selectedYearLevel,
                onValueChange = {},
                placeholder = "Select a year level",
                leadingIcon = Lucide.UserRound,
                modifier = Modifier.menuAnchor(),
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
            text = "Create account",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val next = mutableMapOf<String, String>()
                val normalized = schoolId.trim().uppercase()
                if (normalized.isEmpty()) next["school_id"] = "Student ID is required."
                else if (!SchoolIdPattern.matches(normalized)) next["school_id"] = "Enter a valid STI Student ID."
                if (firstName.isBlank()) next["first_name"] = "First name is required."
                if (lastName.isBlank()) next["last_name"] = "Last name is required."
                if (contactNumber.isBlank()) next["contact_number"] = "Contact number is required."
                else if (!ContactPattern.matches(contactNumber.trim())) next["contact_number"] = "Enter a valid contact number."
                if (selectedProgram.isBlank()) next["program_strand"] = "Program or strand is required."
                if (selectedYearLevel.isBlank()) next["year_grade_level"] = "Year or grade level is required."
                if (password.length < 8) next["password"] = "Password must contain at least 8 characters."
                if (password != confirmPassword) next["confirm_password"] = "Password confirmation does not match."
                errors = next
                if (next.isEmpty()) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                } else {
                    message = "Please correct the highlighted fields."
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Already registered? Back to Login",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { navController.popBackStack() }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
