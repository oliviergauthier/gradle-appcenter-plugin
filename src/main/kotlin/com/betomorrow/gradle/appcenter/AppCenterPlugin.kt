package com.betomorrow.gradle.appcenter

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.betomorrow.gradle.appcenter.extensions.AppCenterAppExtension
import com.betomorrow.gradle.appcenter.extensions.AppCenterExtension
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterTask
import org.gradle.api.Plugin
import org.gradle.api.Project

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

    private fun handleVariant(variant: ApplicationVariant, appCenterConfig: AppCenterExtension, project: Project) {

        appCenterConfig.apps.matching { app ->
            if (app.dimension != null) {
                variant.productFlavors.any { flavor -> flavor.name == app.name && flavor.dimension == app.dimension }
            } else {
                app.name == variant.name
            }
        }.all { appConfig ->
            variant.outputs.withType(ApkVariantOutput::class.java)
                .matching { it.outputType == VariantOutput.MAIN || it.filters.isEmpty() }
                .all { output -> createUploadTask(project, variant, output, appConfig) }
        }
    }

    private fun createUploadTask(
        project: Project,
        variant: ApplicationVariant,
        output: ApkVariantOutput,
        appConfig: AppCenterAppExtension
    ) {
        project.tasks.register(
            "appCenterUpload${variant.name.capitalize()}", UploadAppCenterTask::class.java
        ) { uploadTask ->
            uploadTask.group = APP_CENTER_PLUGIN_GROUP
            uploadTask.description = "Upload ${variant.name} APK to AppCenter"

            uploadTask.apiToken = appConfig.apiToken
            uploadTask.appName = appConfig.appName
            uploadTask.distributionGroups = appConfig.distributionGroups
            uploadTask.ownerName = appConfig.ownerName
            uploadTask.fileProvider = variant.packageApplicationProvider.flatMap { it.outputDirectory.file(output.outputFileName) }
            uploadTask.releaseNotes = appConfig.releaseNotes
            uploadTask.notifyTesters = appConfig.notifyTesters

            if (appConfig.uploadMappingFiles) {
                val mappingFile = variant.mappingFile
                uploadTask.mappingFileProvider = { mappingFile }
            } else {
                uploadTask.mappingFileProvider = { null }
            }
            uploadTask.versionName = variant.versionName
            uploadTask.versionCode = variant.versionCode
            uploadTask.symbols = appConfig.symbols

            uploadTask.mustRunAfter(variant.assembleProvider)
        }
    }

    companion object {
        const val APP_CENTER_EXTENSION_NAME = "appcenter"
        const val APP_CENTER_PLUGIN_GROUP = "AppCenter"
    }

}
