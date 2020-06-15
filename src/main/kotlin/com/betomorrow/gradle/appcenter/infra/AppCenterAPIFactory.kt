package com.betomorrow.gradle.appcenter.infra

import org.gradle.api.Project
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class AppCenterAPIFactory(
    private val project: Project
) {

    fun create(apiToken: String, debug: Boolean): AppCenterAPI {

        val builder = OkHttpBuilder(project)
            .logger(debug)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-API-Token", apiToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.appcenter.ms/v0.1/")
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AppCenterAPI::class.java)
    }

}
