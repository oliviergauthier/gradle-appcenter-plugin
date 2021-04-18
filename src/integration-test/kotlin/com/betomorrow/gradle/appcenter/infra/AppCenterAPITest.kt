package com.betomorrow.gradle.appcenter.infra

import com.betomorrow.gradle.appcenter.AppCenterProperties.API_TOKEN
import com.betomorrow.gradle.appcenter.AppCenterProperties.APP_NAME
import com.betomorrow.gradle.appcenter.AppCenterProperties.OWNER_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class AppCenterAPITest {

    @Test
    fun testPrepareUpload() {
        val project = ProjectBuilder.builder().build()
        val api = AppCenterAPIFactory(project, API_TOKEN, true).createApi()

        val result = api.prepareReleaseUpload(OWNER_NAME, APP_NAME).execute().body()
        println(result)
    }
}
