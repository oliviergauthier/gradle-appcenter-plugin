package com.betomorrow.gradle.appcenter.infra

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Duration

class OkHttpFactory {

    fun create(debug: Boolean) : OkHttpClient {
        val builder = OkHttpClient().newBuilder()

        builder.connectTimeout(getTimeout(CONNECT_TIMEOUT_ENV_NAME))
        builder.readTimeout(getTimeout(READ_TIMEOUT_ENV_NAME))
        builder.writeTimeout(getTimeout(WRITE_TIMEOUT_ENV_NAME))

        if (debug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }

        return builder.build()
    }

    private fun getTimeout(timeoutName: String): Duration {
        val seconds = System.getenv(timeoutName)?.toLongOrNull() ?: DEFAULT_TIMEOUT
        return Duration.ofSeconds(seconds)
    }

    companion object {
        const val DEFAULT_TIMEOUT = 60L
        const val CONNECT_TIMEOUT_ENV_NAME = "http.timeout.connect"
        const val READ_TIMEOUT_ENV_NAME = "http.timeout.read"
        const val WRITE_TIMEOUT_ENV_NAME = "http.timeout.write"
    }
}
