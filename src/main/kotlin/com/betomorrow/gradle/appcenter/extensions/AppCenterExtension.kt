package com.betomorrow.gradle.appcenter.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.lang.Exception

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
            return _apiToken ?: getGlobalConfig("APPCENTER_API_TOKEN", "")
        }
        set(value) {
            this._apiToken = value
        }

    var ownerName: String
        get() {
            return _ownerName ?: getGlobalConfig("APPCENTER_OWNER_NAME", "")
        }
        set(value) {
            this._ownerName = value
        }

    var releaseNotes: Any
        get() {
            return _releaseNotes ?: getGlobalConfig("APPCENTER__RELEASE_NOTES", "")
        }
        set(value) {
            _releaseNotes = value
        }

    var distributionGroups: List<String>
        get() {
            return if (!_distributionGroups.isEmpty())
                _distributionGroups
            else
                getGlobalConfig("APPCENTER_DISTRIBUTION_GROUPS", "").split(",")
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

    private fun getGlobalConfig(name: String, defaultValue: String) : String {
        return try {
            System.getProperty(name)
        } catch (e: Exception) {
            try {
                System.getenv(name)
            } catch (e: Exception) {
                defaultValue
            }
        }

    }

}