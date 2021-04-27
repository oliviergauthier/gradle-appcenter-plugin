package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import javax.inject.Inject

open class UploadAppCenterMappingTask @Inject constructor(
    private val apiToken: String,
    private val ownerName: String,
    private val appName: String,
    private val versionName: String,
    private val versionCode: Int,
    private val mappingFileProvider: () -> File
) : DefaultTask() {

    private var loggerFactory = services[ProgressLoggerFactory::class.java]

    @TaskAction
    fun upload() {
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)

        val loggerMapping = loggerFactory.newOperation("AppCenter")
        loggerMapping.start("AppCenter Upload mapping file", "Step 0/3")
        try {
            val mappingFile = mappingFileProvider.invoke()
            uploader.uploadMapping(mappingFile, versionName, versionCode) {
                loggerMapping.progress(it)
            }
            loggerMapping.completed("AppCenter Upload mapping file completed", false)
        } catch (e: Exception) {
            loggerMapping.completed("AppCenter Upload mapping file failed", true)
        }
    }
}