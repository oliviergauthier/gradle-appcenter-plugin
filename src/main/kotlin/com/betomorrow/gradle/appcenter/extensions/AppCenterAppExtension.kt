package com.betomorrow.gradle.appcenter.extensions

open class AppCenterAppExtension(val name: String, val parent: AppCenterExtension) {

    private var _apiToken: String? = null
    private var _ownerName: String? = null
    private var _distributionGroups: List<String>? = null
    private var _releaseNotes: Any? = null
    private var _notifyTesters: Boolean? = null
    private var _uploadMappingFiles: Boolean? = null
    private var _symbols: List<Any>? = null

    var dimension: String? = null

    var appName: String? = null

    var apiToken: String
        get() = _apiToken ?: parent.apiToken
        set(value) {
            this._apiToken = value
        }

    var ownerName: String
        get() = _ownerName ?: parent.ownerName
        set(value) {
            this._ownerName = value
        }

    var distributionGroups: List<String>
        get() = _distributionGroups ?: parent.distributionGroups
        set(value) {
            this._distributionGroups = value
        }

    var releaseNotes: Any
        get() = _releaseNotes ?: parent.releaseNotes
        set(value) {
            this._releaseNotes = value
        }

    var notifyTesters: Boolean
        get() = _notifyTesters ?: parent.notifyTesters
        set(value) {
            _notifyTesters = value
        }

    var uploadMappingFiles: Boolean
        get() = _uploadMappingFiles ?: parent.uploadMappingFiles
        set(value) {
            _uploadMappingFiles = value
        }

    var symbols: List<Any>
        get() = _symbols ?: parent.symbols
        set(value) {
            _symbols = value
        }

    fun dimension(dimension: String) {
        this.dimension = dimension
    }

    fun appName(appName: String) {
        this.appName = appName
    }
}
