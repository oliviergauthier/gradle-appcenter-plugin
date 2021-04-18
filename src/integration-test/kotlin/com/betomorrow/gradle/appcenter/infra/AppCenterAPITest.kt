package com.betomorrow.gradle.appcenter.infra

import com.betomorrow.gradle.appcenter.AppCenterProperties
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class AppCenterAPITest {

    @Test
    fun testPrepareUpload() {
        val properties = AppCenterProperties()
        val project = ProjectBuilder.builder().build()
        val api = AppCenterAPIFactory(project, properties.apiToken, true).createApi()

        val result = api.prepareReleaseUpload(properties.ownerName, properties.appName).execute().body()
        println(result)
    }
}
