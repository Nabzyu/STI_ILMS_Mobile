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
    fun challengeSurvivesSaveAndRestore() {
        val challenge = LoginChallenge("c-123", "02000123456", "user", "a***@ormoc.sti.edu.ph", 60)
        val saved = with(LoginChallengeSaver) { scope.save(challenge) }!!
        assertEquals(challenge, LoginChallengeSaver.restore(saved))
    }

    @Test
    fun noChallengeSavesNothing() {
        // Nothing saved means rememberSaveable falls back to its initial value (null in the app).
        assertNull(with(LoginChallengeSaver) { scope.save(null) })
    }
}
