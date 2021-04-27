package com.betomorrow.gradle.appcenter

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterPluginTest {

    val properties = AppCenterProperties()

    @Test
    fun testApplyPluginOnAndroidApp36() {
        val result = GradleRunner.create()
            .withProjectDir(File("src/integration-test/resources/AndroidApp3.6"))
            .withArguments(
                "clean",
                "appCenterAssembleAndUploadAlphaDebug",
                "--stacktrace",
                "-PapiToken=${properties.apiToken}",
                "-PownerName=${properties.ownerName}",
                "-PappName=${properties.appName}"
            )
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.output)
    }

    @Test
    fun testApplyPluginOnAndroidApp41() {
        val result = GradleRunner.create()
            .withProjectDir(File("src/integration-test/resources/AndroidApp4.1"))
            .withArguments(
                "clean",
                "appCenterAssembleAndUploadAAlphaDebug",
                "--stacktrace",
                "-PapiToken=${properties.apiToken}",
                "-PownerName=${properties.ownerName}",
                "-PappName=${properties.appName}"
            )
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.output)
    }
}
