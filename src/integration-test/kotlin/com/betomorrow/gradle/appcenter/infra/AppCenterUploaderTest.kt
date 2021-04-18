package com.betomorrow.gradle.appcenter.infra

import com.betomorrow.gradle.appcenter.AppCenterProperties
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterUploaderTest {

    val properties = AppCenterProperties()

    @Test
    fun testUploadApk() {
        val debug = true
        val project = ProjectBuilder.builder().build()
        val apiFactory = AppCenterAPIFactory(project, properties.apiToken, debug)
        val uploader = AppCenterUploader(apiFactory, properties.ownerName, properties.appName)

        val file = File("src/integration-test/resources/test.apk")
        uploader.uploadApk(file, "newVersion", listOf("Sample Group", "Collaborators"), false) {
            println(it)
        }
    }

    @Test
    fun testUploadSymbols() {
        val debug = true
        val project = ProjectBuilder.builder().build()
        val apiFactory = AppCenterAPIFactory(project, properties.apiToken, debug)
        val uploader = AppCenterUploader(apiFactory, properties.ownerName, properties.appName)

        val mappingFile = File("src/integration-test/resources/mapping.txt")
        uploader.uploadMapping(mappingFile, "1.0", 1) {
            println(it)
        }
    }
}
