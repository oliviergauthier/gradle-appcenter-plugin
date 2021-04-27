package com.betomorrow.gradle.appcenter.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.lang.Exception

open class AppCenterExtension(val project: Project) {

    private var _apiToken: String? = null
    private var _ownerName: String? = null
    private var _distributionGroups: List<String>? = null
    private var _releaseNotes: Any? = null
    private var _notifyTesters: Boolean? = null
    private var _symbols: List<Any>? = null

    var apps: NamedDomainObjectContainer<AppCenterAppExtension> = project.container(AppCenterAppExtension::class.java) {
        AppCenterAppExtension(it, this)
    }

    var apiToken: String
        get() = _apiToken ?: getGlobalConfig("APPCENTER_API_TOKEN", "")
        set(value) {
            this._apiToken = value
        }

    var ownerName: String
        get() = _ownerName ?: getGlobalConfig("APPCENTER_OWNER_NAME", "")
        set(value) {
            this._ownerName = value
        }

    var releaseNotes: Any
        get() = _releaseNotes ?: getGlobalConfig("APPCENTER_RELEASE_NOTES", "")
        set(value) {
            _releaseNotes = value
        }

    var distributionGroups: List<String>
        get() = _distributionGroups ?: getGlobalConfig("APPCENTER_DISTRIBUTION_GROUPS", "").split(",")
        set(value) {
            _distributionGroups = value
        }

    var notifyTesters: Boolean
        get() = _notifyTesters ?: getGlobalConfig("APPCENTER_NOTIFY_TESTERS", "false").toBoolean()
        set(value) {
            _notifyTesters = value
        }

    var symbols: List<Any>
        get() = _symbols ?: getGlobalConfig("APPCENTER_SYMBOLS", "").split(",")
        set(value) {
            _symbols = value
        }

    var uploadMappingFiles: Boolean = true

    fun apps(action: Action<NamedDomainObjectContainer<AppCenterAppExtension>>) {
        action.execute(apps)
    }

    fun findByFlavor(name: String, dimension: String?): AppCenterAppExtension? {
        return apps.firstOrNull { it.name == name && dimension == it.dimension }
    }

    fun findByBuildVariant(name: String): AppCenterAppExtension? {
        return apps.firstOrNull { it.name == name }
    }

    private fun getGlobalConfig(name: String, defaultValue: String): String {
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
