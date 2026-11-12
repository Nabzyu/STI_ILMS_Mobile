package com.example.stismartlib.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.session.AppLock
import com.example.stismartlib.ui.components.AppDialog
import com.example.stismartlib.ui.components.AppDialogType

/**
 * Puts the fingerprint lock screen over [content] while the app is locked (the app underneath
 * keeps its place), and asks once after a Student/Faculty sign-in whether to turn the lock on.
 */
@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val repository = rememberLibraryRepository()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val locked = repository.isAppLocked
    var unlockError by remember { mutableStateOf<String?>(null) }
    var prompting by remember { mutableStateOf(false) }

    fun unlock() {
        if (prompting || activity == null) return
        prompting = true
        unlockError = null
        activity.requestDeviceUnlock(
            onSuccess = {
                prompting = false
                repository.unlockApp()
            },
            onError = { message ->
                prompting = false
                unlockError = message
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hidden from TalkBack while locked; the lock screen below takes all touches.
        Box(modifier = if (locked) Modifier.fillMaxSize().clearAndSetSemantics {} else Modifier.fillMaxSize()) {
            content()
        }
        if (locked) {
            LaunchedEffect(Unit) {
                unlockError = null
                unlock()
            }
            BackHandler { activity?.moveTaskToBack(true) }
            AppLockScreen(
                userName = repository.session.userName,
                schoolId = repository.session.schoolId,
                errorMessage = unlockError,
                onUnlock = ::unlock,
                onUsePassword = repository::logout,
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) awaitPointerEvent().changes.forEach { it.consume() }
                    }
                },
            )
        }
    }

    val offer = repository.appLockOfferPending && !locked
    LaunchedEffect(offer) {
        if (offer && !AppLock.shouldOffer(repository.canUseAppLock(), null, activity?.let(::canUseDeviceUnlock) == true)) {
            repository.dismissAppLockOffer()
        }
    }
    AppDialog(
        visible = offer && activity != null && canUseDeviceUnlock(activity),
        // "Not now" (or tapping outside) is remembered for this account; it can be changed in the menu.
        onDismiss = { repository.setAppLockEnabled(false) },
        type = AppDialogType.CONFIRM,
        title = "Use fingerprint to unlock?",
        message = "You'll stay signed in on this phone for 30 days. Turn this on to ask for your " +
            "fingerprint or phone screen lock when the app opens, so no one else can use your account.",
        confirmText = "Turn on",
        dismissText = "Not now",
        onConfirm = {
            repository.dismissAppLockOffer()
            activity?.requestDeviceUnlock(
                onSuccess = { repository.setAppLockEnabled(true) },
                onError = {}, // Closed or failed: not recorded, so it is offered again next sign-in.
            )
        },
    )
}
