package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.extensions.ArtifactType
import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import com.betomorrow.gradle.appcenter.utils.truncate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

open class UploadAppCenterBinaryTask @Inject constructor(
    private val apiToken: String,
    private val ownerName: String,
    private val appName: String,
    private val distributionGroups: List<String>,
    private val notifyTesters: Boolean,
    private val releaseNotes: Any?,
    private val artifactType: ArtifactType,
    private val fileProvider: () -> File
) : DefaultTask() {

    private var loggerFactory = services[ProgressLoggerFactory::class.java]

    @TaskAction
    fun upload() {
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)

        val loggerRelease = loggerFactory.newOperation("AppCenter")
        loggerRelease.start("AppCenter Upload apk", "Step 0/7")
        uploader.uploadApk(fileProvider(), toReleaseNotes(releaseNotes), distributionGroups, notifyTesters, artifactType) {
            loggerRelease.progress(it)
        }
        loggerRelease.completed("AppCenter Upload apk completed", false)
    }

    private fun toReleaseNotes(releaseNotes: Any?): String {
        return when (releaseNotes) {
            is File -> releaseNotes.readText()
            is Path -> releaseNotes.toFile().readText()
            is String -> releaseNotes
            else -> releaseNotes?.toString().orEmpty()
        }.truncate(MAX_RELEASE_NOTES_LENGTH)
    }

    companion object {
        const val MAX_RELEASE_NOTES_LENGTH = 5000
    }
}