package com.example.stismartlib.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.R
import com.example.stismartlib.navigation.Screen
import com.example.stismartlib.ui.components.StiMarkTile
import com.example.stismartlib.ui.theme.ElmsCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavHostController) {
    val repository = rememberLibraryRepository()
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.86f) }

    LaunchedEffect(Unit) {
        launch {
            alpha.animateTo(1f, tween(durationMillis = 700))
        }
        launch {
            scale.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        }
        delay(900)
        val restored = repository.restoreSession()
        if (restored) {
            val destination = if (repository.session.userType.isStaff) {
                Screen.StaffHome.route
            } else {
                Screen.Dashboard.create(repository.session.userType)
            }
            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            StiMarkTile(size = 112.dp, contentDescription = stringResource(R.string.system_full_name))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.system_identity_line),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.system_full_descriptor),
                color = ElmsCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }
    }
}
