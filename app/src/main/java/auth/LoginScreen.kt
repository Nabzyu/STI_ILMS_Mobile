package com.example.stismartlib.ui.auth

import com.composables.icons.lucide.*

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.StiMark
import com.example.stismartlib.ui.feedback.userFacingMessage
import com.example.stismartlib.ui.theme.ElmsCyan
import com.example.stismartlib.ui.theme.EyebrowStyle
import kotlinx.coroutines.launch

private val SchoolIdPattern = Regex("^[A-Z0-9][A-Z0-9._-]{2,49}$")

@Composable
fun LoginScreen(navController: NavHostController) {
    val repository = rememberLibraryRepository()
    val scope = rememberCoroutineScope()
    var schoolId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var schoolIdError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var banner by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        StiMark()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "WELCOME BACK",
            style = EyebrowStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Use your school ID and password. Your workspace opens based on your account.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        if (banner != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ElmsBanner(message = banner.orEmpty(), tone = ElmsBannerTone.Error)
        }
        Spacer(modifier = Modifier.height(28.dp))
        PortalField(
            label = "School ID",
            value = schoolId,
            onValueChange = { schoolId = it.uppercase() },
            placeholder = "Enter your school ID",
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
                .sizeIn(minHeight = 48.dp)
                .clickable { showPassword = !showPassword }
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
                val normalized = schoolId.trim().uppercase()
                schoolIdError = when {
                    normalized.isEmpty() -> "School ID is required."
                    !SchoolIdPattern.matches(normalized) -> "Enter a valid STI school ID."
                    else -> null
                }
                passwordError = if (password.isEmpty()) "Password is required." else null
                if (schoolIdError != null || passwordError != null) {
                    banner = null
                    return@PortalButton
                }
                isSubmitting = true
                banner = null
                scope.launch {
                    try {
                        repository.login(normalized, password)
                        password = ""
                        val destination = if (repository.session.userType.isStaff) {
                            Screen.StaffHome.route
                        } else {
                            Screen.Dashboard.create(repository.session.userType)
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
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
        Spacer(modifier = Modifier.height(28.dp))
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
