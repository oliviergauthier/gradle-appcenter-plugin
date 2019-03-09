package com.betomorrow.gradle.appcenter.infra

import org.gradle.internal.impldep.org.junit.Ignore
import org.junit.jupiter.api.Test
import java.io.File

class AppCenterUploaderTest {

    @Ignore
    @Test
    fun testUploadApk() {
        val api = AppCenterAPIFactory().create(apiToken, true)
        val httpClient = OkHttpFactory().create(true)
        val uploader = AppCenterUploader(api, httpClient, ownerName, appName)

        val file = File(apkPath)
        uploader.upload(file, "newVersion", listOf("Sample Group", "Collaborators"))
    }

    companion object {
        val apiToken: String = ""
        val ownerName: String = ""
        val appName : String = ""
        val apkPath : String = ""
    }

}