package com.example.stismartlib.ui.auth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Fingerprint (or face) with the phone's PIN, pattern or password as fallback.
 * `BIOMETRIC_STRONG or DEVICE_CREDENTIAL` is not supported before Android 11, so older phones
 * use `BIOMETRIC_WEAK or DEVICE_CREDENTIAL` (Android's documented combination for those versions).
 */
fun appLockAuthenticators(sdkInt: Int = Build.VERSION.SDK_INT): Int =
    if (sdkInt >= Build.VERSION_CODES.R) BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    else BIOMETRIC_WEAK or DEVICE_CREDENTIAL

/** True when the phone has a fingerprint or a screen lock that can unlock the app. */
fun canUseDeviceUnlock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(appLockAuthenticators()) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Text for a failed unlock, or null when the user just closed the prompt (nothing to show).
 * Lockouts and a missing fingerprint/screen lock point the user to their password.
 */
fun unlockErrorMessage(errorCode: Int, systemMessage: CharSequence?): String? = when (errorCode) {
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    BiometricPrompt.ERROR_CANCELED,
    -> null
    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
    -> "Too many attempts. Try again later, or sign in with your password."
    BiometricPrompt.ERROR_NO_BIOMETRICS,
    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    -> "This phone has no fingerprint or screen lock set up. Sign in with your password."
    else -> systemMessage?.toString()?.takeIf { it.isNotBlank() }
        ?: "Couldn't check your fingerprint. Try again, or sign in with your password."
}

/** Shows the system fingerprint/PIN prompt. [onError] gets null when the user closed it. */
fun showDeviceUnlockPrompt(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onError: (String?) -> Unit,
) {
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) =
                onError(unlockErrorMessage(errorCode, errString))
            // onAuthenticationFailed (finger not recognised) is shown by the system prompt itself.
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(appLockAuthenticators())
        .build()
    prompt.authenticate(info)
}

/** The hosting activity, needed by [BiometricPrompt]. */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is android.content.ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

/** Asks for a fingerprint/PIN; reports an error message, or null when the user closed the prompt. */
fun FragmentActivity.requestDeviceUnlock(onSuccess: () -> Unit, onError: (String?) -> Unit) {
    if (!canUseDeviceUnlock(this)) {
        onError(unlockErrorMessage(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL, null))
        return
    }
    showDeviceUnlockPrompt(
        activity = this,
        title = "Unlock STI Library",
        subtitle = "Use your fingerprint or phone screen lock",
        onSuccess = onSuccess,
        onError = onError,
    )
}
