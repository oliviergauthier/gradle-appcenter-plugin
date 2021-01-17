package com.betomorrow.gradle.appcenter

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterPluginTest {

    @Test
    fun testApplyPlugin() {
        val result = GradleRunner.create()
            .withProjectDir(File("src/integration-test/resources/MyApplication"))
            .withArguments(
                "clean",
                "appCenterAssembleAndUploadAlphaDebug",
                "--stacktrace",
                "-PapiToken=${AppCenterProperties.API_TOKEN}",
                "-PownerName=${AppCenterProperties.OWNER_NAME}",
                "-PappName=${AppCenterProperties.APP_NAME}"
            )
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.getOutput())
    }
}
