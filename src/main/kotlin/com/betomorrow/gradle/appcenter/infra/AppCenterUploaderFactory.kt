package com.betomorrow.gradle.appcenter.infra

import org.gradle.api.Project

class AppCenterUploaderFactory(
    private val project: Project
) {

    fun create(apiToken: String, ownerName: String, appName: String): AppCenterUploader {
        when {
            apiToken.isBlank() -> throw IllegalArgumentException("apiToken must be defined")
            ownerName.isBlank() -> throw IllegalArgumentException("ownerName must be defined")
            appName.isBlank() -> throw IllegalArgumentException("appName must be defined")
        }

        val apiFactory = AppCenterAPIFactory(project, apiToken, true)
        return AppCenterUploader(apiFactory, ownerName, appName)
    }
}
