package com.example.stismartlib.session

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.example.stismartlib.data.LibraryRepository
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

private const val ACTIVE_WINDOW_MS = 60_000L
private const val MIN_RENEW_GAP_MS = 120_000L
private const val CHECK_EVERY_MS = 30_000L

/**
 * Renew only while the user is interacting, like the web's SessionActivity: an idle phone
 * still hits the API's 10-minute idle limit, and an active one outlives the 15-minute JWT.
 */
fun shouldRenew(nowMs: Long, lastInteractionMs: Long, lastRenewMs: Long): Boolean =
    nowMs - lastInteractionMs < ACTIVE_WINDOW_MS && nowMs - lastRenewMs >= MIN_RENEW_GAP_MS

@Composable
fun SessionKeepAlive(repository: LibraryRepository, content: @Composable () -> Unit) {
    val lastInteraction = remember { AtomicLong(System.currentTimeMillis()) }
    val signedIn = repository.session.isAuthenticated

    LaunchedEffect(signedIn) {
        if (!signedIn) return@LaunchedEffect
        // Login just issued a token, so count it as a renewal.
        var lastRenew = System.currentTimeMillis()
        while (true) {
            delay(CHECK_EVERY_MS)
            val now = System.currentTimeMillis()
            if (shouldRenew(now, lastInteraction.get(), lastRenew) && repository.renewSession()) {
                lastRenew = now
            }
        }
    }

    Box(
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    // Initial pass + no consume: observe touches without stealing them.
                    awaitPointerEvent(PointerEventPass.Initial)
                    lastInteraction.set(System.currentTimeMillis())
                }
            }
        },
    ) {
        content()
    }
}
