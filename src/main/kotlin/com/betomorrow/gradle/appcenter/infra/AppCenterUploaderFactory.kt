package com.betomorrow.gradle.appcenter.infra

class AppCenterUploaderFactory {

    fun create(apiToken: String, ownerName: String, appName: String): AppCenterUploader {
        val api = AppCenterAPIFactory().create(apiToken, true)
        val httpClient = OkHttpFactory().create(true)
        return AppCenterUploader(api, httpClient, ownerName, appName)
    }

}