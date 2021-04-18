package com.betomorrow.gradle.appcenter.infra

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AppCenterAPI {

    /**
     * curl
     * -X POST
     * --header 'Content-Type: application/json'
     * --header 'Accept: application/json'
     * --header 'X-API-Token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName}/{appName}/uploads/releases/'
     */
    @POST("apps/{ownerName}/{appName}/uploads/releases/")
    fun prepareReleaseUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String
    ): Call<PrepareReleaseUploadResponse>

    /**
     * curl -X PATCH
     * --header 'Content-Type: application/json'
     * --header 'Accept: application/json'
     * --header 'X-API-Token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
     * -d '{ "status": "committed"  }'
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName/{appName}/uploads/releases/{upload_id}'
     */
    @PATCH("apps/{ownerName}/{appName}/uploads/releases/{uploadId}")
    fun commitReleaseUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Path("uploadId") uploadId: String,
        @Body status: CommitReleaseUploadRequest
    ): Call<CommitReleaseUploadResponse>

    @GET("apps/{ownerName}/{appName}/uploads/releases/{uploadId}")
    fun getUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Path("uploadId") uploadId: String
    ): Call<GetUploadResponse>

    /**
     * curl -X PATCH
     * --header 'Content-Type: application/json'
     * --header 'Accept: application/json'
     * --header 'X-API-Token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
     * -d '{ "destination_name": "QA Testers", "release_notes": "Example new release via the APIs" }'
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName}/{appName}/releases/2'
     */
    @PATCH("apps/{ownerName}/{appName}/releases/{releaseId}")
    fun distribute(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Path("releaseId") releaseId: String,
        @Body request: DistributeRequest
    ): Call<Void>

    /**
     * curl
     * -X POST
     * --header 'Content-Type: application/json'
     * --header 'Accept: application/json'
     * --header 'X-API-Token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName}/{appName}/symbol_uploads'
     */
    @POST("apps/{ownerName}/{appName}/symbol_uploads")
    fun prepareSymbolUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Body request: PrepareSymbolUploadRequest
    ): Call<PrepareSymbolUploadResponse>

    /**
     * curl -X PATCH
     * --header 'Content-Type: application/json'
     * --header 'Accept: application/json'
     * --header 'X-API-Token: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
     * -d '{ "status": "committed"  }'
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName/{appName}/symbol_uploads/{uploadId}'
     */
    @PATCH("apps/{ownerName}/{appName}/symbol_uploads/{uploadId}")
    fun commitSymbolUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Path("uploadId") uploadId: String,
        @Body status: CommitSymbolUploadRequest
    ): Call<CommitSymbolUploadResponse>
}

class PrepareReleaseUploadResponse(
    @SerializedName("id") val id: String,
    @SerializedName("upload_domain") val uploadDomain: String,
    @SerializedName("token") val token: String,
    @SerializedName("url_encoded_token") val urlEncodedToken: String,
    @SerializedName("package_asset_id") val packageAssetId: String
)

class CommitReleaseUploadRequest(
    @SerializedName("upload_status") val uploadStatus: String
)

class CommitReleaseUploadResponse(
    @SerializedName("id") val id: String,
    @SerializedName("upload_status") val uploadStatus: String
)

class GetUploadResponse(
    @SerializedName("id") val id: String,
    @SerializedName("upload_status") val uploadStatus: String,
    @SerializedName("error_details") val errorDetails: String?,
    @SerializedName("release_distinct_id") val releaseId: String?
)

class PrepareSymbolUploadRequest(
    @SerializedName("symbol_type") val symbolType: String,
    @SerializedName("client_callback") val clientCallback: String? = null,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("build") val build: String? = null,
    @SerializedName("version") val version: String? = null
)

class PrepareSymbolUploadResponse(
    @SerializedName("symbol_upload_id") val symbolUploadId: String,
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("expiration_date") val expirationDate: String? = null
)

class CommitSymbolUploadRequest(
    val status: String
)

class CommitSymbolUploadResponse(
    @SerializedName("symbol_upload_id") val symbolUploadId: String
)

class DistributeRequest(
    @SerializedName("distribution_group_name") val distributionGroupName: String? = null,
    @SerializedName("distribution_group_id") val distributionGroupId: String? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    @SerializedName("destination_id") val destinationId: String? = null,
    @SerializedName("destination_type") val destinationType: String? = null,
    @SerializedName("release_notes") val releaseNotes: String? = null,
    @SerializedName("mandatory_update") val mandatoryUpdate: Boolean = false,
    @SerializedName("destinations") val destinations: List<Destination>? = null,
    @SerializedName("build") val build: Build? = null,
    @SerializedName("notify_testers") val notifyTesters: Boolean = false
) {

    class Destination(
        @SerializedName("name") val name: String? = null,
        @SerializedName("id") val id: String? = null
    )

    class Build(
        @SerializedName("branch") val branch: String? = null,
        @SerializedName("commit_hash") val commitHash: String? = null,
        @SerializedName("commit_message") val commitMessage: String? = null
    )
}
