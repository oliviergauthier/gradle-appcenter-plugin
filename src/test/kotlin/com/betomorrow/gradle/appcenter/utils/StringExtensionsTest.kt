package com.betomorrow.gradle.appcenter.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringExtensionsTest {

    @Test
    fun `test truncate with maxLength smaller than String length`() {
        assertEquals("Hello", "Hello World".truncate(5))
    }

    @Test
    fun `test truncate with maxLength larger than String length`() {
        assertEquals("Hello World", "Hello World".truncate(5000))
    }

    @Test
    fun `test truncate with maxLength equals String length`() {
        assertEquals("Hello", "Hello".truncate(5))
    }
}
