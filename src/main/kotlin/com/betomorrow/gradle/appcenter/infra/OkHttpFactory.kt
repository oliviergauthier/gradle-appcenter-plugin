package com.betomorrow.gradle.appcenter.infra

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpFactory {

    fun create(debug: Boolean) : OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        builder.readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (debug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }

        return builder.build()
    }

    companion object {

        const val DEFAULT_TIMEOUT_SECONDS: Long = 20
    }
}
