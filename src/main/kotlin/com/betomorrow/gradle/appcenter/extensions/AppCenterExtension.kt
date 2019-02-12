package com.betomorrow.gradle.appcenter.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class AppCenterExtension(val project: Project) {

    var _apiToken: String? = null
    var _ownerName: String? = null

    var _distributionGroups: List<String> = emptyList()
    var _releaseNotes: Any? = null

    var apps: NamedDomainObjectContainer<AppCenterAppExtension> = project.container(AppCenterAppExtension::class.java) {
        AppCenterAppExtension(it, this)
    }

    var apiToken: String
        get() {
            return _apiToken ?: System.getProperty("APPCENTER_API_TOKEN", "")
        }
        set(value) {
            this._apiToken = value
        }

    var ownerName: String
        get() {
            return _ownerName ?: System.getProperty("APPCENTER_OWNER_NAME", "")
        }
        set(value) {
            this._apiToken = value
        }

    var releaseNotes: Any
        get() {
            return _releaseNotes ?: System.getProperty("APPCENTER_DISTRIBUTE_RELEASE_NOTES", "")
        }
        set(value) {
            _releaseNotes = value
        }

    var distributionGroups: List<String>
        get() {
            return if (!_distributionGroups.isEmpty())
                _distributionGroups
            else
                System.getProperty("APPCENTER_DISTRIBUTE_DESTINATION", "").split(",")
        }
        set(value) {
            _distributionGroups = value
        }

    fun apps(action: Action<NamedDomainObjectContainer<AppCenterAppExtension>>) {
        action.execute(apps)
    }

    fun findByFlavor(name: String, dimension: String): AppCenterAppExtension? {
        return apps.firstOrNull { it.name == name && dimension == it.dimension }
    }

    fun findByBuildVariant(name: String): AppCenterAppExtension? {
        return apps.firstOrNull { it.name == name }
    }

}