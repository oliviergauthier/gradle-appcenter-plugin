package com.betomorrow.gradle.appcenter.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class AppCenterExtension(val project: Project) {

    lateinit var apiToken : String
    lateinit var ownerName : String

    var distributionGroups : List<String> = emptyList()
    var releaseNotes : Any? = null

    var apps : NamedDomainObjectContainer<AppCenterAppExtension> = project.container(AppCenterAppExtension::class.java) {
        AppCenterAppExtension(it, this)
    }

    fun apps(action: Action<NamedDomainObjectContainer<AppCenterAppExtension>>) {
        action.execute(apps)
    }

    fun findByFlavor(name: String, dimension: String): AppCenterAppExtension? {
        return  apps.firstOrNull { it.name == name && dimension == it.dimension }
    }

    fun findByBuildVariant(name: String) : AppCenterAppExtension ? {
        return apps.firstOrNull { it.name == name }
    }

}