package com.betomorrow.gradle.appcenter.infra

import org.gradle.testfixtures.ProjectBuilder

class AppCenterAPITest {

//    @Test
    fun testPrepareUpload() {
        val project = ProjectBuilder.builder().build()
        val api = AppCenterAPIFactory(project).create(AppCenterUploaderTest.apiToken, true)

        val result = api.prepareReleaseUpload(ownerName, appName).execute().body()
        println(result)
    }

    companion object {
        val apiToken: String = ""
        val ownerName: String = ""
        val appName : String = ""
        val apkPath : String = ""
    }

}