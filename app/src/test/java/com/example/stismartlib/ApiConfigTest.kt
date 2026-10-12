package com.example.stismartlib

import com.example.stismartlib.network.ApiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiConfigTest {
    @Test
    fun baseUrlEndsWithSlash() {
        assertTrue(ApiConfig.baseUrl.endsWith("/"))
    }

    @Test
    fun baseUrlTargetsHostedWebApi() {
        assertEquals("https://sti-ormoc-smart-library.vercel.app/", ApiConfig.baseUrl)
    }
}
