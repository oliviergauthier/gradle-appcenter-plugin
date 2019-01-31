package com.betomorrow.gradle.appcenter.extensions

open class AppCenterAppExtension(val name: String, val parent: AppCenterExtension) {

    private var _apiToken: String? = null

    private var _ownerName: String? = null

    private var _distributionGroups: List<String>? = null

    var dimension: String? = null

    lateinit var appName: String

    var appCenterId: String? = null


    var apiToken: String
        get() {
            return _apiToken ?: parent.apiToken
        }
        set(value) {
            this._apiToken = value
        }

    var ownerName: String
        get() {
            return _ownerName ?: parent.ownerName
        }
        set(value) {
            this._ownerName = value
        }


    var distributionGroups: List<String>
        get() {
            return _distributionGroups ?: parent.distributionGroups
        }
        set(value) {
            this.distributionGroups = value
        }

    fun dimension(dimension: String) {
        this.dimension = dimension
    }

    fun appName(appName: String) {
        this.appName = appName
    }

    fun appCenterId(id : String) {
        this.appCenterId = id
    }

}