package com.example.stismartlib

import androidx.compose.runtime.saveable.SaverScope
import com.example.stismartlib.data.LoginChallenge
import com.example.stismartlib.ui.auth.LoginChallengeSaver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginChallengeSaverTest {
    private val scope = SaverScope { true }

    @Test
    fun codeStepSurvivesRotation() {
        val challenge = LoginChallenge("6f1c2a8e-1b2c-4d3e-8f90-123456789abc", "ju***@ormoc.sti.edu.ph", 600, 42)
        val saved = with(LoginChallengeSaver) { scope.save(challenge) }!!
        assertEquals(listOf<Any>(challenge.challengeId, challenge.emailHint, 600, 42), saved)
        assertEquals(challenge, LoginChallengeSaver.restore(saved))
    }

    @Test
    fun noCodeStepSavesNothing() {
        // Nothing is saved, so the screen starts without a code step after a restore.
        assertNull(with(LoginChallengeSaver) { scope.save(null) })
        assertNull(LoginChallengeSaver.restore(listOf("partial")))
    }
}
