package com.betomorrow.gradle.appcenter.utils

import kotlin.math.min

fun String.truncate(maxLength: Int) : String {
    return this.substring(0, min(this.length, maxLength))
}