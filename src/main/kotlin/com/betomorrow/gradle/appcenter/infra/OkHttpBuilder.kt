package com.betomorrow.gradle.appcenter.infra

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

class OkHttpBuilder(
    private val project: Project
) {

    private var logger = false
    private val interceptors = mutableListOf<Interceptor>()

    fun logger(enable: Boolean): OkHttpBuilder {
        logger = enable
        return this
    }

    fun addInterceptor(interceptor: Interceptor): OkHttpBuilder {
        interceptors.add(interceptor)
        return this
    }

    fun addInterceptor(interceptor: (Interceptor.Chain) -> Response): OkHttpBuilder {
        interceptors.add(LambdaInterceptor(interceptor))
        return this
    }

    fun build(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()

        builder.connectTimeout(getTimeout(CONNECT_TIMEOUT_ENV_NAME), TimeUnit.SECONDS)
        builder.readTimeout(getTimeout(READ_TIMEOUT_ENV_NAME), TimeUnit.SECONDS)
        builder.writeTimeout(getTimeout(WRITE_TIMEOUT_ENV_NAME), TimeUnit.SECONDS)

        if (logger) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }

        interceptors.forEach { builder.addInterceptor(it) }

        return builder.build()
    }

    private fun getTimeout(timeoutName: String): Long {
        return getProperty(timeoutName) ?: getEnvVar(timeoutName) ?: DEFAULT_TIMEOUT
    }

    private fun getProperty(propertyName: String): Long? {
        return project.properties.getOrDefault(propertyName, null).toString().toLongOrNull()
    }

    private fun getEnvVar(propertyName: String): Long? {
        return System.getenv(propertyName)?.replace(".", "_")?.toLongOrNull()
    }

    companion object {
        const val DEFAULT_TIMEOUT = 60L
        const val CONNECT_TIMEOUT_ENV_NAME = "http.timeout.connect"
        const val READ_TIMEOUT_ENV_NAME = "http.timeout.read"
        const val WRITE_TIMEOUT_ENV_NAME = "http.timeout.write"
    }

}

class LambdaInterceptor(
    private val lambda: (Interceptor.Chain) -> Response
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return lambda(chain)
    }
}
