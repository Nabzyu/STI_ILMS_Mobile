package com.example.stismartlib

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import com.example.stismartlib.ui.auth.appLockAuthenticators
import com.example.stismartlib.ui.auth.unlockErrorMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricUnlockTest {
    @Test
    fun android11AndLaterUseStrongBiometricsWithPinFallback() {
        assertEquals(BIOMETRIC_STRONG or DEVICE_CREDENTIAL, appLockAuthenticators(30))
        assertEquals(BIOMETRIC_STRONG or DEVICE_CREDENTIAL, appLockAuthenticators(36))
    }

    @Test
    fun olderPhonesUseTheSupportedWeakPlusPinCombination() {
        assertEquals(BIOMETRIC_WEAK or DEVICE_CREDENTIAL, appLockAuthenticators(26))
        assertEquals(BIOMETRIC_WEAK or DEVICE_CREDENTIAL, appLockAuthenticators(29))
    }

    @Test
    fun closingThePromptShowsNothing() {
        assertNull(unlockErrorMessage(BiometricPrompt.ERROR_USER_CANCELED, "Cancelled"))
        assertNull(unlockErrorMessage(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "Cancel"))
        assertNull(unlockErrorMessage(BiometricPrompt.ERROR_CANCELED, "Cancelled"))
    }

    @Test
    fun lockoutPointsToPassword() {
        val message = unlockErrorMessage(BiometricPrompt.ERROR_LOCKOUT, "Too many attempts")
        assertTrue(message!!.contains("sign in with your password"))
        assertEquals(message, unlockErrorMessage(BiometricPrompt.ERROR_LOCKOUT_PERMANENT, null))
    }

    @Test
    fun missingFingerprintAndScreenLockPointsToPassword() {
        val message = unlockErrorMessage(BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL, null)
        assertTrue(message!!.contains("no fingerprint or screen lock"))
        assertEquals(message, unlockErrorMessage(BiometricPrompt.ERROR_NO_BIOMETRICS, null))
    }

    @Test
    fun otherErrorsUseSystemTextOrFallback() {
        assertEquals("Sensor dirty", unlockErrorMessage(BiometricPrompt.ERROR_VENDOR, "Sensor dirty"))
        assertTrue(unlockErrorMessage(BiometricPrompt.ERROR_TIMEOUT, " ")!!.startsWith("Couldn't check"))
    }
}
