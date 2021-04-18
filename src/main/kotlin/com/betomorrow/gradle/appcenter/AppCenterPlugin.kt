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
        val appCenterApp = variant.productFlavors.mapNotNull {
            appCenterExtension.findByFlavor(
                it.name,
                it.dimension
            )
        }.firstOrNull() ?: appCenterExtension.findByBuildVariant(variant.name)

        appCenterApp?.let {
            val outputDirectory = variant.packageApplicationProvider.get().outputDirectory.get().asFile
            val assembleTask = variant.assembleProvider.get()

            variant.outputs.all { output ->
                if (output is ApkVariantOutput) {
                    val filterIdentifiersCapitalized = output.filters.joinToString("") { it.identifier.capitalize() }
                    val taskSuffix = "${variant.name.capitalize()}$filterIdentifiersCapitalized"

                    val mappingFileProvider = if (appCenterApp.uploadMappingFiles) {
                        { variant.mappingFileProvider.get().firstOrNull() }
                    } else {
                        { null }
                    }

                    val uploadTask = project.tasks.register(
                        "appCenterUpload$taskSuffix",
                        UploadAppCenterTask::class.java,
                        appCenterApp.apiToken,
                        appCenterApp.ownerName,
                        appCenterApp.appName,
                        variant.versionName,
                        variant.versionCode,
                        { File(outputDirectory, output.outputFileName) },
                        mappingFileProvider,
                        appCenterApp.symbols,
                        appCenterApp.releaseNotes,
                        appCenterApp.distributionGroups,
                        appCenterApp.notifyTesters
                    ).apply {
                        configure { task ->
                            task.group = APP_CENTER_PLUGIN_GROUP
                            task.description = "Upload ${variant.name} APK to AppCenter"

                            task.mustRunAfter(assembleTask)
                        }
                    }

                    project.tasks.register(
                        "appCenterAssembleAndUpload$taskSuffix"
                    ) { task ->
                        task.group = APP_CENTER_PLUGIN_GROUP
                        task.description = "Assemble and upload ${variant.name} APK to AppCenter"
                        task.dependsOn(uploadTask, assembleTask)
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
