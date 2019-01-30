package com.betomorrow.gradle.appcenter

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AppCenterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            plugins.withType(AppPlugin::class.java) {

                val androidExtension = extensions.getByName("android") as AppExtension
                androidExtension.productFlavors.whenObjectAdded { flavor ->
                    flavor.setMatchingFallbacks()
                    println("Product ${flavor.javaClass}")
                }

                afterEvaluate {
                    val androidExtension = extensions.getByName("android") as AppExtension
                    androidExtension.applicationVariants.forEach { variant ->
                        println("uploadAppCenter${variant.name.capitalize()} : ${variant.outputs.firstOrNull()?.outputFile?.absolutePath}")
                    }
                }

            }
        }
    }

}