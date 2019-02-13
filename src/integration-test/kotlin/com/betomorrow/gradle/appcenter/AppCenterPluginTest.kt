package com.betomorrow.gradle.appcenter

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterPluginTest {

    @Test
    fun testApplyPlugin() {

        val result = GradleRunner.create()
            .withProjectDir(File("/Users/olivier/projets/amelia/td-amelia2-phone"))
//            .withProjectDir(File("src/integration-test/resources/MyApplication"))
            .withArguments("clean", "tasks", "--stacktrace", "-PownerName=ogauthierbto",  "-PapiToken=dbf35aa0107b408840e3d8d7216ab8f1f7fbd8eb")
            .withPluginClasspath()
            .withDebug(true)
            .build()

        println(result.getOutput())

    }


}