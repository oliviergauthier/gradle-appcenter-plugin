package com.betomorrow.gradle.appcenter.utils

fun String.truncate(maxLength: Int) =
    substring(0, maxLength.coerceAtMost(length))
