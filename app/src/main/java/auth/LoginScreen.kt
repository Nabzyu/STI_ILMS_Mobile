package com.example.stismartlib.ui.auth
import com.composables.icons.lucide.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.ui.components.LibraryMark
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.ShapeXl
import com.example.stismartlib.ui.theme.AccentYellow
import com.example.stismartlib.ui.theme.EyebrowStyle
import com.example.stismartlib.ui.theme.PrimaryBlue

private val SchoolIdPattern = Regex("^[A-Z0-9][A-Z0-9._-]{2,49}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var selectedUserType by remember { mutableStateOf(UserType.STUDENT.displayName) }
    var schoolId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var roleMenuOpen by remember { mutableStateOf(false) }
    var schoolIdError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        LibraryMark()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "WELCOME BACK",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Login",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (banner != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = banner.orEmpty(),
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeXl)
                    .border(1.dp, PrimaryBlue, ShapeXl)
                    .background(AccentYellow)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text("Login as", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = roleMenuOpen,
            onExpandedChange = { roleMenuOpen = it }
        ) {
            PortalField(
                label = "",
                value = selectedUserType,
                onValueChange = {},
                placeholder = "Student",
                leadingIcon = Lucide.UserRound,
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = roleMenuOpen,
                onDismissRequest = { roleMenuOpen = false }
            ) {
                UserType.allDisplayNames.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) },
                        onClick = {
                            selectedUserType = option
                            roleMenuOpen = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        PortalField(
            label = "School ID",
            value = schoolId,
            onValueChange = { schoolId = it.uppercase() },
            placeholder = "Enter your Student, Faculty, or Staff ID",
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
            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
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
                .sizeIn(minHeight = 48.dp)
                .clickable { showPassword = !showPassword }
        ) {
            Checkbox(
                checked = showPassword,
                onCheckedChange = { showPassword = it },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue, uncheckedColor = PrimaryBlue)
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
            text = "Login",
            onClick = {
                val normalized = schoolId.trim().uppercase()
                schoolIdError = when {
                    normalized.isEmpty() -> "School ID is required."
                    !SchoolIdPattern.matches(normalized) -> "Enter a valid STI school ID."
                    else -> null
                }
                passwordError = if (password.isEmpty()) "Password is required." else null
                if (schoolIdError == null && passwordError == null) {
                    val userType = UserType.fromDisplayName(selectedUserType)
                    navController.navigate(Screen.Dashboard.create(userType)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    banner = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 52.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            textDecoration = TextDecoration.Underline,
                            color = PrimaryBlue
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
