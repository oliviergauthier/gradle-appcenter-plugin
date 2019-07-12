package com.betomorrow.gradle.appcenter.infra

import org.gradle.api.Project

class AppCenterUploaderFactory(
    private val project: Project
) {

    fun create(apiToken: String, ownerName: String, appName: String): AppCenterUploader {
        val api = AppCenterAPIFactory(project).create(apiToken, true)
        val httpClient = OkHttpBuilder(project).logger(true).build()
        return AppCenterUploader(api, httpClient, ownerName, appName)
    }

}