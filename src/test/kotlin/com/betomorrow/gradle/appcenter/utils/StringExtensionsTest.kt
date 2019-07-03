package com.betomorrow.gradle.appcenter.utils

import org.junit.jupiter.api.Test

class StringExtensionsTest {

    @Test
    fun `test truncate`() {
        assert("Hello World".truncate(5000) == "Hello World")
        assert("Hello World".truncate(5) == "Hello")
    }

}