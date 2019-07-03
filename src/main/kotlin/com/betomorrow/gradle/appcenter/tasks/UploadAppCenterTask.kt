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
    var notifyTesters: Boolean = false

    var distributionGroups: List<String> = emptyList()

    lateinit var fileProvider: () -> File
    var releaseNotes: Any? = null

    private var logger = services[ProgressLoggerFactory::class.java]
        .newOperation("AppCenter")

    @TaskAction
    fun upload() {
        logger.start("AppCenter Upload", "Step 0/4")
        val uploader = AppCenterUploaderFactory().create(apiToken, ownerName, appName)
        uploader.upload(fileProvider(), toReleaseNotes(releaseNotes), distributionGroups, notifyTesters) {
            logger.progress(it)
        }
        logger.completed("AppCenter Upload completed", false)
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