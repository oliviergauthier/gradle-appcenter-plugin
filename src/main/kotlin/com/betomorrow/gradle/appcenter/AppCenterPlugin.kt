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

            val appCenterExtension = extensions.create(APP_CENTER_EXTENSION_NAME, AppCenterExtension::class.java, this)

            plugins.withId("com.android.application") {
                val androidExtension = extensions.getByName("android") as AppExtension
                androidExtension.applicationVariants.all { variant ->
                    handleVariant(variant, appCenterExtension, this)
                }
            }
        }

    }

    private fun handleVariant(variant: ApplicationVariant, appCenterExtension: AppCenterExtension, project: Project) {

        appCenterExtension.apps.matching { app ->
            if (app.dimension != null) {
                variant.productFlavors.any { flavor -> flavor.name == app.name && flavor.dimension == app.dimension }
            } else {
                app.name == variant.name
            }
        }.all { appCenterApp ->
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
                        uploadTask.fileProvider = variant.packageApplicationProvider.map { File(it.outputDirectory, output.outputFileName) }
                        uploadTask.releaseNotes = appCenterApp.releaseNotes
                        uploadTask.notifyTesters = appCenterApp.notifyTesters

                        if (appCenterExtension.uploadMappingFile) {
                            val mappingFile = variant.mappingFile
                            uploadTask.mappingFileProvider = { mappingFile }
                        } else {
                            uploadTask.mappingFileProvider = { null }
                        }
                        uploadTask.versionName = variant.versionName
                        uploadTask.versionCode = variant.versionCode
                        uploadTask.symbols = appCenterApp.symbols

                        uploadTask.mustRunAfter(variant.assembleProvider)
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
