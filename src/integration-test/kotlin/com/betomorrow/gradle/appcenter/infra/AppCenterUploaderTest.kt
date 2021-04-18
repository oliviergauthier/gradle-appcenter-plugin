package com.betomorrow.gradle.appcenter.infra

import com.betomorrow.gradle.appcenter.AppCenterProperties
import com.betomorrow.gradle.appcenter.AppCenterProperties.API_TOKEN
import com.betomorrow.gradle.appcenter.AppCenterProperties.APP_NAME
import com.betomorrow.gradle.appcenter.AppCenterProperties.MAPPING_PATH
import com.betomorrow.gradle.appcenter.AppCenterProperties.OWNER_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterUploaderTest {

    @Test
    fun testUploadApk() {
        val debug = true
        val project = ProjectBuilder.builder().build()
        val apiFactory = AppCenterAPIFactory(project, API_TOKEN, debug)
        val uploader = AppCenterUploader(apiFactory, OWNER_NAME, APP_NAME)

        val file = File(AppCenterProperties.APK_PATH)
        uploader.uploadApk(file, "newVersion", listOf("Sample Group", "Collaborators"), false) {
            println(it)
        }
    }

    @Test
    fun testUploadSymbols() {
        val debug = true
        val project = ProjectBuilder.builder().build()
        val apiFactory = AppCenterAPIFactory(project, API_TOKEN, debug)
        val uploader = AppCenterUploader(apiFactory, OWNER_NAME, APP_NAME)

        val mappingFile = File(MAPPING_PATH)
        uploader.uploadSymbols(mappingFile, "AndroidProguard", "1.0", "1") {
            println(it)
        }
    }
}
