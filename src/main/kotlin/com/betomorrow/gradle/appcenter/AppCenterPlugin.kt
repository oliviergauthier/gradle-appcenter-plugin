package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
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

            variant.outputs.all { output ->
                if (output is ApkVariantOutput) {
                    project.tasks.register(
                        "appCenterUpload${variant.name.capitalize()}", UploadAppCenterTask::class.java
                    ) { uploadTask ->
                        uploadTask.group = APP_CENTER_PLUGIN_GROUP
                        uploadTask.description = "Upload ${variant.name} APK to AppCenter"

                        uploadTask.apiToken = appCenterApp.apiToken
                        uploadTask.appName = appCenterApp.appName
                        uploadTask.distributionGroups = appCenterApp.distributionGroups
                        uploadTask.ownerName = appCenterApp.ownerName
                        uploadTask.fileProvider = { File(outputDirectory, output.outputFileName) }
                        uploadTask.releaseNotes = appCenterApp.releaseNotes
                        uploadTask.notifyTesters = appCenterApp.notifyTesters

                        if (appCenterApp.uploadMappingFiles) {
                            val mappingFile = variant.mappingFile
                            uploadTask.mappingFileProvider = { mappingFile }
                        } else {
                            uploadTask.mappingFileProvider = { null }
                        }
                        uploadTask.versionName = variant.versionName
                        uploadTask.versionCode = variant.versionCode
                        uploadTask.symbols = appCenterApp.symbols

                        uploadTask.mustRunAfter(assembleTask)
                    }
                }
            }
        }
    }

    companion object {
        const val APP_CENTER_EXTENSION_NAME = "appcenter"
        const val APP_CENTER_PLUGIN_GROUP = "AppCenter"
    }

}
