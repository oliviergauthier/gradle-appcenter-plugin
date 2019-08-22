package com.betomorrow.gradle.appcenter.infra

import org.gradle.internal.impldep.org.junit.Ignore
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterUploaderTest {

    @Ignore
    @Test
    fun testUploadApk() {
        val project = ProjectBuilder.builder().build()
        val api = AppCenterAPIFactory(project).create(apiToken, true)
        val httpClient = OkHttpBuilder(project).logger(true).build()
        val uploader = AppCenterUploader(api, httpClient, ownerName, appName)

        val file = File(apkPath)
        uploader.uploadApk(file, "newVersion", listOf("Sample Group", "Collaborators"), false)
    }

    companion object {
        val apiToken: String = ""
        val ownerName: String = ""
        val appName : String = ""
        val apkPath : String = ""
    }

}