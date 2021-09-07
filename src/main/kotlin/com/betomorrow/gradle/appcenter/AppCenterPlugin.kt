package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.betomorrow.gradle.appcenter.extensions.AppCenterExtension
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterApkTask
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterMappingTask
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterSymbolsTask
import com.betomorrow.gradle.appcenter.utils.capitalized
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
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
                    val filterIdentifiersCapitalized = output.filters.joinToString("") { it.identifier.capitalized() }

                    val taskSuffix = "${variant.name.capitalized()}$filterIdentifiersCapitalized"

                    val appCenterAppTasks = mutableListOf<TaskProvider<out Task>>()

                    appCenterAppTasks.add(project.tasks.register(
                        "appCenterUploadApk$taskSuffix",
                        UploadAppCenterApkTask::class.java,
                        appCenterApp.apiToken,
                        appCenterApp.ownerName,
                        appCenterApp.appName,
                        appCenterApp.distributionGroups,
                        appCenterApp.notifyTesters,
                        appCenterApp.releaseNotes,
                        { File(outputDirectory, output.outputFileName) }
                    ).apply {
                        configure { task ->
                            task.group = APP_CENTER_PLUGIN_GROUP
                            task.description = "Upload ${variant.name} APK to AppCenter"
                            task.mustRunAfter(assembleTask)
                        }
                    })

                    if (appCenterApp.uploadMappingFiles) {
                        appCenterAppTasks.add(project.tasks.register(
                            "appCenterUploadMapping$taskSuffix",
                            UploadAppCenterMappingTask::class.java,
                            appCenterApp.apiToken,
                            appCenterApp.ownerName,
                            appCenterApp.appName,
                            variant.versionName,
                            variant.versionCode,
                            { variant.mappingFileProvider.get().first() }
                        ).apply {
                            configure { task ->
                                task.group = APP_CENTER_PLUGIN_GROUP
                                task.description = "Upload ${variant.name} Mapping to AppCenter"
                                task.mustRunAfter(assembleTask)
                            }
                        })
                    }

                    if (appCenterApp.symbols.isNotEmpty()) {
                        appCenterAppTasks.add(project.tasks.register(
                            "appCenterUploadSymbols$taskSuffix",
                            UploadAppCenterSymbolsTask::class.java,
                            appCenterApp.apiToken,
                            appCenterApp.ownerName,
                            appCenterApp.appName,
                            variant.versionName,
                            variant.versionCode,
                            { appCenterApp.symbols }
                        ).apply {
                            configure { task ->
                                task.group = APP_CENTER_PLUGIN_GROUP
                                task.description = "Upload ${variant.name} Symbols to AppCenter"
                                task.mustRunAfter(assembleTask)
                            }
                        })
                    }

                    val uploadTasks = project.tasks.register(
                        "appCenterUpload$taskSuffix"
                    ) { task ->
                        task.group = APP_CENTER_PLUGIN_GROUP
                        task.description = "Upload ${variant.name} to AppCenter"
                        task.dependsOn(*(appCenterAppTasks.toTypedArray()))
                    }

                    project.tasks.register(
                        "appCenterAssembleAndUpload$taskSuffix"
                    ) { task ->
                        task.group = APP_CENTER_PLUGIN_GROUP
                        task.description =
                            "Assemble and upload ${variant.name} APK to AppCenter. (Deprecated, call ${assembleTask.name} explicitly then use appCenterUpload$taskSuffix task)"
                        task.dependsOn(uploadTasks, assembleTask)
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
