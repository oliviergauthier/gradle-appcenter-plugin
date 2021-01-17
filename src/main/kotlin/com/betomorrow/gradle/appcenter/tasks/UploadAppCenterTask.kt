package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import com.betomorrow.gradle.appcenter.utils.truncate
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject


open class UploadAppCenterTask @Inject constructor(
    private val apiToken: String,
    private val ownerName: String,
    private val appName: String,
    private val versionName: String,
    private val versionCode: Int,
    private val fileProvider: () -> File,
    private val mappingFileProvider: () -> File?,
    private val symbols: List<Any>,
    private val releaseNotes: Any?,
    private val distributionGroups: List<String>,
    private val notifyTesters: Boolean
) : DefaultTask() {

    private var loggerFactory = services[ProgressLoggerFactory::class.java]

    @TaskAction
    fun upload() {
        val loggerRelease = loggerFactory.newOperation("AppCenter")
        loggerRelease.start("AppCenter Upload apk", "Step 0/7")
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)
        uploader.uploadApk(fileProvider(), toReleaseNotes(releaseNotes), distributionGroups, notifyTesters) {
            loggerRelease.progress(it)
        }
        loggerRelease.completed("AppCenter Upload apk completed", false)

        val mappingFile = mappingFileProvider.invoke()
        if (mappingFile != null) {
            val loggerMapping = loggerFactory.newOperation("AppCenter")
            loggerMapping.start("AppCenter Upload mapping file", "Step 0/3")
            uploader.uploadSymbols(
                mappingFile,
                "AndroidProguard",
                versionName,
                versionCode.toString()
            ) {
                loggerMapping.progress(it)
            }
            loggerMapping.completed("AppCenter Upload mapping file completed", false)
        }

        symbols.forEach { symbol ->
            val loggerSymbol = loggerFactory.newOperation("AppCenter")
            loggerSymbol.start("AppCenter Upload symbol $symbol ", "Step 0/3")
            try {
                uploader.uploadSymbols(toSymbolFile(symbol), "Breakpad", versionName, versionCode.toString()) {
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