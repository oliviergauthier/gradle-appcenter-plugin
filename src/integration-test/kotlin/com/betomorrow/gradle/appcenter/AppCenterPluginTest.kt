package com.betomorrow.gradle.appcenter

import org.gradle.internal.impldep.org.junit.Rule
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class AppCenterPluginTest {

    @Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun testApplyPlugin() {
        testProjectDir.create()
        val buildFile = testProjectDir.newFile("build.gradle")

        buildFile.appendText("""
            plugins {
                id 'com.betomorrow.appcenter'
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("tasks", "--stacktrace", "--all")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.getOutput())

    }


}