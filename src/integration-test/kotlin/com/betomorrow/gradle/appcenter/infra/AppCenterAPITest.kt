package com.betomorrow.gradle.appcenter.infra

import org.junit.jupiter.api.Test

class AppCenterAPITest {

//    @Test
    fun testPrepareUpload() {
        val api = AppCenterAPIFactory().create(AppCenterUploaderTest.apiToken, true)

        val result = api.prepare(ownerName, appName).execute().body()
        println(result)
    }

    companion object {
        val apiToken: String = ""
        val ownerName: String = ""
        val appName : String = ""
        val apkPath : String = ""
    }

}