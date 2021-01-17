package com.betomorrow.gradle.appcenter.infra

import org.gradle.api.Project
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppCenterAPIFactory(
    private val project: Project,
    private val apiToken: String,
    private val debug: Boolean = false
) {

    fun createApi() =
        create<AppCenterAPI>("https://api.appcenter.ms/v0.1/")

    fun createUploadApi(uploadUrl: String) =
        create<UploadAPI>(uploadUrl)

    fun createSymbolsApi() =
        create<SymbolsAPI>("https://localhost/")

    private inline fun <reified T> create(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)

    private fun createHttpClient() =
        OkHttpBuilder(project)
            .logger(debug)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-Token", apiToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
}
