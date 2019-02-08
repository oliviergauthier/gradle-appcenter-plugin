package com.betomorrow.gradle.appcenter.infra

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppCenterAPIFactory {

    fun create(apiToken: String, debug: Boolean): AppCenterAPI {

        val builder = OkHttpClient().newBuilder()
        builder.readTimeout(10, TimeUnit.SECONDS)
        builder.connectTimeout(5, TimeUnit.SECONDS)

        if (debug) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(interceptor)
        }

        builder.addInterceptor { chain ->
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