package com.betomorrow.gradle.appcenter.infra

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Url

interface SymbolsAPI {

    @PUT
    @Headers("x-ms-blob-type: BlockBlob")
    fun uploadSymbols(
        @Url url: String,
        @Body file: RequestBody
    ): Call<Void>
}
