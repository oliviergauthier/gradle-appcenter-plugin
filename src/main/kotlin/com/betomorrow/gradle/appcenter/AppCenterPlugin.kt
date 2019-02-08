package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
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
                    val appCenterApp = variant.productFlavors.map {
                        appCenterExtension.findByFlavor(
                            it.dimension,
                            it.name
                        )
                    }.firstOrNull { it != null } ?: appCenterExtension.findByBuildVariant(variant.name)

                    appCenterApp?.let {
                        variant.outputs.first()?.let { output ->
                            project.tasks.register(
                                "upload${variant.name.capitalize()}", UploadAppCenterTask::class.java
                            ) { t ->
                                t.group = APP_CENTER_PLUGIN_GROUP
                                t.description = "Upload apk to AppCenter"

                                t.apiToken = appCenterApp.apiToken
                                t.appName = appCenterApp.appName
                                t.distributionGroups = appCenterApp.distributionGroups
                                t.ownerName = appCenterApp.ownerName
                                t.file = output.outputFile
                                t.releaseNotes = appCenterApp.releaseNotes
                            }
                        }
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