package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import com.betomorrow.gradle.appcenter.utils.truncate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path

open class UploadAppCenterTask : DefaultTask() {

    @Input
    lateinit var apiToken: String
    @Input
    lateinit var ownerName: String
    @Input
    lateinit var appName: String
    @Input
    lateinit var versionName: String
    @Input
    var versionCode: Int = 0
    @Input
    var notifyTesters: Boolean = false
    @Input
    var distributionGroups: List<String> = emptyList()
    @Input
    var symbols : List<Any> = emptyList()

    @Input
    lateinit var fileProvider: () -> File
    @Input
    @Optional
    lateinit var mappingFileProvider: () -> File?
    @Input
    @Optional
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
        if (mappingFile != null) {
            val loggerMapping = loggerFactory.newOperation("AppCenter")
            loggerMapping.start("AppCenter Upload mapping file", "Step 0/4")
            uploader.uploadSymbols(mappingFile, "AndroidProguard", versionName, versionCode.toString()) {
                loggerMapping.progress(it)
            }
            loggerMapping.completed("AppCenter Upload mapping completed", false)
        }

        symbols.forEach {
            val loggerSymbol = loggerFactory.newOperation("AppCenter")
            loggerSymbol.start("AppCenter Upload symbol $it ", "Step 0/4")
            try {
                uploader.uploadSymbols(toSymbolFile(it), "Breakpad", versionName, versionCode.toString()) {
                    loggerSymbol.progress(it)
                }
                loggerSymbol.completed("AppCenter Upload symbol $it succeed", false)
            } catch (e : Exception) {
                loggerSymbol.completed("AppCenter Upload symbol $it failed", true)
            }
        }

    }

    private fun toReleaseNotes(releaseNotes: Any?): String {
        return when (releaseNotes) {
            is File -> releaseNotes.readText()
            is Path -> releaseNotes.toFile().readText()
            is String -> releaseNotes
            else -> releaseNotes?.toString().orEmpty()
        }.truncate(MAX_RELEASE_NOTES_LENGTH)
    }

    private fun toSymbolFile(symbolFile : Any): File {
        return when (symbolFile) {
            is File -> symbolFile
            is Path -> symbolFile.toFile()
            is String -> project.file(symbolFile)
            else -> throw IllegalArgumentException("$symbolFile is not supported")
        }
    }

    companion object {
        const val MAX_RELEASE_NOTES_LENGTH = 5000
    }

}