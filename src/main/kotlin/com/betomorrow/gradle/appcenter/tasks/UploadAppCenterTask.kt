package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import com.betomorrow.gradle.appcenter.utils.truncate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject


open class UploadAppCenterTask @Inject constructor(
    @Input val apiToken: String,
    @Input val ownerName: String,
    @Input val appName: String,
    @Input val versionName: String,
    @Input val versionCode: Int,
    @InputFile val fileProvider: () -> File,
    @Input val uploadMappingFiles: Boolean,
    @InputFile val mappingFileProvider: () -> File,
    @Input val symbols: List<Any>,
    @Input val releaseNotes: Any?,
    @Input val distributionGroups: List<String>,
    @Input val notifyTesters: Boolean
) : DefaultTask() {

    private var loggerFactory = services[ProgressLoggerFactory::class.java]

    @TaskAction
    fun upload() {
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)

        val loggerRelease = loggerFactory.newOperation("AppCenter")
        loggerRelease.start("AppCenter Upload apk", "Step 0/7")
        uploader.uploadApk(fileProvider(), toReleaseNotes(releaseNotes), distributionGroups, notifyTesters) {
            loggerRelease.progress(it)
        }
        loggerRelease.completed("AppCenter Upload apk completed", false)

        if (uploadMappingFiles) {
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

        symbols.forEach { symbol ->
            val loggerSymbol = loggerFactory.newOperation("AppCenter")
            loggerSymbol.start("AppCenter Upload symbol $symbol ", "Step 0/3")
            try {
                uploader.uploadSymbols(toSymbolFile(symbol), versionName, versionCode) {
                    loggerSymbol.progress(it)
                }
                loggerSymbol.completed("AppCenter Upload symbol $symbol completed", false)
            } catch (e: Exception) {
                loggerSymbol.completed("AppCenter Upload symbol $symbol failed", true)
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

    private fun toSymbolFile(symbolFile: Any): File {
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