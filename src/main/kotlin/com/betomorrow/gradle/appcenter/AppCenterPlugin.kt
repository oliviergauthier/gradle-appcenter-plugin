package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.betomorrow.gradle.appcenter.extensions.AppCenterAppExtension
import com.betomorrow.gradle.appcenter.extensions.AppCenterExtension
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class AppCenterPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        with(project) {

            extensions.create(APP_CENTER_EXTENSION_NAME, AppCenterExtension::class.java, this)

            afterEvaluate { p ->

                val androidExtension = extensions.getByName("android") as AppExtension
                val appCenterExtension = extensions.getByName(APP_CENTER_EXTENSION_NAME) as AppCenterExtension
                androidExtension.applicationVariants.whenObjectAdded { variant ->
                    handleVariant(variant, appCenterExtension, p)
                }

                androidExtension.applicationVariants.forEach { variant ->
                    handleVariant(variant, appCenterExtension, p)
                }
            }

        }

    }

    private fun handleVariant(variant: ApplicationVariant, appCenterExtension: AppCenterExtension, project: Project) {

        val appCenterApp = variant.productFlavors.map {
            appCenterExtension.findByFlavor(
                it.name,
                it.dimension
            )
        }.firstOrNull { it != null } ?: appCenterExtension.findByBuildVariant(variant.name)

        appCenterApp?.let {

            val outputDirectory = variant.packageApplicationProvider.get().outputDirectory
            val assembleTask = variant.assembleProvider.get()
            val mappingFile = variant.mappingFile

            variant.outputs.all { output ->
                if (output is ApkVariantOutput) {
                    val varianNameCapitalized = variant.name.capitalize()
                    project.tasks.register(
                        "appCenterUpload$varianNameCapitalized", UploadAppCenterTask::class.java
                    ) { uploadTask ->
                        uploadTask.description = "Upload apk to AppCenter"
                        setupUploadTask(uploadTask, appCenterApp, outputDirectory, output, mappingFile, variant)
                        uploadTask.mustRunAfter(assembleTask)
                    }

                    project.tasks.register(
                        "appCenterAssembleAndUpload$varianNameCapitalized", UploadAppCenterTask::class.java
                    ) { uploadTask ->
                        uploadTask.description = "Assemble and upload apk to AppCenter"
                        setupUploadTask(uploadTask, appCenterApp, outputDirectory, output, mappingFile, variant)
                        uploadTask.dependsOn(assembleTask)
                    }
                }
            }
        }
    }

    private fun setupUploadTask(
        uploadTask: UploadAppCenterTask,
        appCenterApp: AppCenterAppExtension,
        outputDirectory: File?,
        output: ApkVariantOutput,
        mappingFile: File?,
        variant: ApplicationVariant
    ) {
        uploadTask.group = APP_CENTER_PLUGIN_GROUP

        uploadTask.apiToken = appCenterApp.apiToken
        uploadTask.appName = appCenterApp.appName
        uploadTask.distributionGroups = appCenterApp.distributionGroups
        uploadTask.ownerName = appCenterApp.ownerName
        uploadTask.fileProvider = { File(outputDirectory, output.outputFileName) }
        uploadTask.releaseNotes = appCenterApp.releaseNotes
        uploadTask.notifyTesters = appCenterApp.notifyTesters

        uploadTask.mappingFileProvider = { mappingFile }
        uploadTask.versionName = variant.versionName
        uploadTask.versionCode = variant.versionCode
        uploadTask.symbols = appCenterApp.symbols
    }

    companion object {
        const val APP_CENTER_EXTENSION_NAME = "appcenter"
        const val APP_CENTER_PLUGIN_GROUP = "AppCenter"
    }

}
