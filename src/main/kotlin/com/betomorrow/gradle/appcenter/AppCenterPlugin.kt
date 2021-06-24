package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.api.ApkVariantImpl
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.betomorrow.gradle.appcenter.extensions.AppCenterExtension
import com.betomorrow.gradle.appcenter.extensions.ArtifactType
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterBinaryTask
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterMappingTask
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterSymbolsTask
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

    private fun handleVariant(variantA: ApplicationVariant, appCenterExtension: AppCenterExtension, project: Project) {
        val variant = variantA as? ApkVariantImpl ?: return
        val assembleTask = variant.assembleProvider.get()
        val bundleTask = variant.variantData.taskContainer.bundleTask!!.get()

        val appCenterApps = variant.productFlavors.flatMap {
            appCenterExtension.findByFlavor(
                it.name,
                it.dimension
            )
        }.ifEmpty { appCenterExtension.findByBuildVariant(variant.name) }

        appCenterApps.forEach { appCenterApp ->
            variant.outputs.withType(ApkVariantOutput::class.java) { output ->
                val outputFilePath = when (appCenterApp.artifactType) {
                    ArtifactType.APK -> variant.getFinalArtifact(InternalArtifactType.APK)
                        .get().asPath + "/" + output.outputFileName
                    ArtifactType.AAB -> variant.getFinalArtifact(InternalArtifactType.BUNDLE)
                        .get().asPath
                }
                val dependantTask = when (appCenterApp.artifactType) {
                    ArtifactType.APK -> assembleTask
                    ArtifactType.AAB -> bundleTask
                }

                val filterIdentifiersCapitalized =
                    output.filters.joinToString("") { it.identifier.capitalize() }
                val taskSuffix = "${variant.name.capitalize()}$filterIdentifiersCapitalized"

                val appCenterAppTasks = mutableListOf<TaskProvider<out Task>>()

                appCenterAppTasks.add(project.tasks.register(
                    "appCenterUploadBinary$taskSuffix",
                    UploadAppCenterBinaryTask::class.java,
                    appCenterApp.apiToken,
                    appCenterApp.ownerName,
                    appCenterApp.appName,
                    appCenterApp.distributionGroups,
                    appCenterApp.notifyTesters,
                    appCenterApp.releaseNotes,
                    appCenterApp.artifactType,
                    { File(outputFilePath) }
                ).apply {
                    configure { task ->
                        task.group = APP_CENTER_PLUGIN_GROUP
                        task.description = "Upload ${variant.name} APK to AppCenter"

                        task.mustRunAfter(dependantTask)
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
                            task.mustRunAfter(dependantTask)
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
                            task.mustRunAfter(dependantTask)
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

                project.tasks.register(
                    "appCenterBundleAndUpload$taskSuffix"
                ) { task ->
                    task.group = APP_CENTER_PLUGIN_GROUP
                    task.description =
                        "Bundle and upload ${variant.name} APK to AppCenter. (Deprecated, call ${assembleTask.name} explicitly then use appCenterUpload$taskSuffix task)"
                    task.dependsOn(uploadTasks, bundleTask)
                }
            }
        }
    }

    companion object {
        const val APP_CENTER_EXTENSION_NAME = "appcenter"
        const val APP_CENTER_PLUGIN_GROUP = "AppCenter"
    }
}
