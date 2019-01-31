package com.betomorrow.gradle.appcenter.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class UploadAppCenterTask : DefaultTask() {

    lateinit var apiToken : String
    lateinit var ownerName : String
    lateinit var appName: String

//    lateinit var apkFile : File

    var distributionGroups : List<String> = emptyList()

    @TaskAction
    fun upload() {
        println("App : ")
        println("- apiToken : $apiToken")
        println("- ownerName : $ownerName")
        println("- appName : $appName")
        println("- distributionGroups : ${distributionGroups.joinToString(",")}")
//        println("- apkFile : $apkFile")
    }

}