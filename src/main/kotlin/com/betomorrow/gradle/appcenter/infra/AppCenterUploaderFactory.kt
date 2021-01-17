package com.betomorrow.gradle.appcenter.infra

import org.gradle.api.Project

class AppCenterUploaderFactory(
    private val project: Project
) {

    fun create(apiToken: String, ownerName: String, appName: String): AppCenterUploader {
        val apiFactory = AppCenterAPIFactory(project, apiToken, true)
        return AppCenterUploader(apiFactory, ownerName, appName)
    }
}
