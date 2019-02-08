package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

open class UploadAppCenterTask : DefaultTask() {

    lateinit var apiToken: String
    lateinit var ownerName: String
    lateinit var appName: String
    var distributionGroups: List<String> = emptyList()

    lateinit var file: File
    var releaseNotes: Any? = null

    @TaskAction
    fun upload() {
        val uploader = AppCenterUploaderFactory().create(apiToken, ownerName, appName)
        uploader.upload(file, toReleaseNotes(releaseNotes), distributionGroups)
    }

    fun toReleaseNotes(releaseNotes: Any?): String {
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
        }
    }

}