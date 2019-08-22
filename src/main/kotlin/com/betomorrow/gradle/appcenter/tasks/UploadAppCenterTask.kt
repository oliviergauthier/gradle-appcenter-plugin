package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import com.betomorrow.gradle.appcenter.utils.truncate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path

open class UploadAppCenterTask : DefaultTask() {

    lateinit var apiToken: String
    lateinit var ownerName: String
    lateinit var appName: String
    lateinit var versionName: String
    var versionCode: Int = 0
    var notifyTesters: Boolean = false

    var distributionGroups: List<String> = emptyList()

    lateinit var fileProvider: () -> File
    lateinit var mappingFileProvider: () -> File?
    var releaseNotes: Any? = null

    private var loggerFactory = services[ProgressLoggerFactory::class.java]


    @TaskAction
    fun upload() {
        val loggerRelease = loggerFactory.newOperation("AppCenter")
        loggerRelease.start("AppCenter Upload apk", "Step 0/4")
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)
        uploader.uploadApk(fileProvider(), toReleaseNotes(releaseNotes), distributionGroups, notifyTesters) {
            loggerRelease.progress(it)
        }
        loggerRelease.completed("AppCenter Upload completed", false)

        val mappingFile = mappingFileProvider()
        if (mappingFile != null){
            val loggerMapping = loggerFactory.newOperation("AppCenter")
            loggerMapping.start("AppCenter Upload mapping file", "Step 0/4")
            uploader.uploadSymbols(mappingFile, versionName, versionCode.toString()) {
                loggerMapping.progress(it)
            }
            loggerMapping.completed("AppCenter Upload mapping completed", false)
        }
    }

    private fun toReleaseNotes(releaseNotes: Any?): String {
        return when (releaseNotes) {
            is File -> {
                return releaseNotes.readText()
            }
            is Path -> {
                return releaseNotes.toFile().readText()
            }
            is String -> {
                releaseNotes
            }
            else -> {
                ""
            }
        }.truncate(MAX_RELEASE_NOTES_LENGTH)
    }

    companion object {
        const val MAX_RELEASE_NOTES_LENGTH = 5000
    }

}