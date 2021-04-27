package com.betomorrow.gradle.appcenter.infra

import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UploadAPI {

    @POST("/upload/set_metadata/{packageAssetId}")
    fun setMetadata(
        @Path("packageAssetId") packageAssetId: String,
        @Query("file_name") fileName: String,
        @Query("file_size") fileSize: Long,
        @Query("token") token: String,
        @Query("content_type") contentType: String
    ): Call<SetMetaDataResponse>

    @POST("/upload/upload_chunk/{packageAssetId}")
    fun uploadChunk(
        @Path("packageAssetId") packageAssetId: String,
        @Query("block_number") chunkId: String,
        @Query("token") token: String,
        @Body chunk: RequestBody
    ): Call<UploadChunkResponse>

    @POST("/upload/finished/{packageAssetId}")
    fun finishUpload(
        @Path("packageAssetId") packageAssetId: String,
        @Query("token") token: String
    ): Call<FinishUploadResponse>

    class SetMetaDataResponse(
        @SerializedName("id") val id: String,
        @SerializedName("chunk_size") val chunkSize: Long,
        @SerializedName("chunk_list") val chunkList: Array<String>
    )
}

class UploadChunkResponse

class FinishUploadResponse
