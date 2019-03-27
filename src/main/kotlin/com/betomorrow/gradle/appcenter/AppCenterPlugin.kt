package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.betomorrow.gradle.appcenter.extensions.AppCenterExtension
import com.betomorrow.gradle.appcenter.tasks.UploadAppCenterTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class AppCenterPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        with(project) {

            extensions.create(APP_CENTER_EXTENSION_NAME, AppCenterExtension::class.java, this)

            afterEvaluate { p ->

                val androidExtension = extensions.getByName("android") as AppExtension
                val appCenterExtension = extensions.getByName(APP_CENTER_EXTENSION_NAME) as AppCenterExtension
                androidExtension.applicationVariants.whenObjectAdded { variant ->
                    handleVariant(variant, appCenterExtension, project)
                }

                androidExtension.applicationVariants.forEach { variant ->
                    handleVariant(variant, appCenterExtension, project)
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

            variant.outputs.first()?.let { output ->
                val task = project.tasks.register(
                    "appCenterUpload${variant.name.capitalize()}", UploadAppCenterTask::class.java
                ) { t ->
                    t.group = APP_CENTER_PLUGIN_GROUP
                    t.description = "Upload apk to AppCenter"

                    t.apiToken = appCenterApp.apiToken
                    t.appName = appCenterApp.appName
                    t.distributionGroups = appCenterApp.distributionGroups
                    t.ownerName = appCenterApp.ownerName
                    t.file = output.outputFile
                    t.releaseNotes = appCenterApp.releaseNotes

                    t.dependsOn("assemble${variant.name.capitalize()}")
                }.get()
            }
        }
    }

    companion object {
        const val APP_CENTER_EXTENSION_NAME = "appcenter"
        const val APP_CENTER_PLUGIN_GROUP = "AppCenter"
    }

}