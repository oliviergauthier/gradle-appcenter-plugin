package com.betomorrow.gradle.appcenter.tasks

import com.betomorrow.gradle.appcenter.infra.AppCenterUploaderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

open class UploadAppCenterSymbolsTask @Inject constructor(
    @Input val apiToken: String,
    @Input val ownerName: String,
    @Input val appName: String,
    @Input val versionName: String,
    @Input val versionCode: Int,
    @Input val symbolsProvider: () -> List<Any>
) : DefaultTask() {

    private var loggerFactory = services[ProgressLoggerFactory::class.java]

    @TaskAction
    fun upload() {
        val uploader = AppCenterUploaderFactory(project).create(apiToken, ownerName, appName)

        symbolsProvider().forEach { symbol ->
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

    private fun toSymbolFile(symbolFile: Any): File {
        return when (symbolFile) {
            is File -> symbolFile
            is Path -> symbolFile.toFile()
            is String -> project.file(symbolFile)
            else -> throw IllegalArgumentException("$symbolFile is not supported")
        }
    }
}