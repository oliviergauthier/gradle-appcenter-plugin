package com.betomorrow.gradle.appcenter

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterPluginTest {

    @Test
    fun testApplyPlugin() {

        val result = GradleRunner.create()
            .withProjectDir(File("src/integration-test/resources/MyApplication"))
            .withArguments("clean", "uploadRelease", "--stacktrace", "-PownerName=",  "-PapiToken=")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.getOutput())

    }


}