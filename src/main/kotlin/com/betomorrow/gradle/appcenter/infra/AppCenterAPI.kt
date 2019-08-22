package com.betomorrow.gradle.appcenter.infra

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
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
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName}/{appName}/release_uploads'
     */
    @POST("apps/{ownerName}/{appName}/release_uploads")
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
     * 'https://api.appcenter.ms/v0.1/apps/{ownerName/{appName}/release_uploads/{upload_id}'
     */
    @PATCH("apps/{ownerName}/{appName}/release_uploads/{uploadId}")
    fun commitReleaseUpload(
        @Path("ownerName") ownerName: String,
        @Path("appName") appName: String,
        @Path("uploadId") uploadId: String,
        @Body status : CommitReleaseUploadRequest
    ) : Call<CommitReleaseUploadResponse>

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
    ) : Call<Void>

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
        @Body status : CommitSymbolUploadRequest
    ) : Call<CommitSymbolUploadResponse>
}

open class PrepareReleaseUploadResponse(
    @SerializedName("upload_id") val uploadId: String,
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("asset_id") val assetId: String?,
    @SerializedName("asset_domain") val assetDomain: String?,
    @SerializedName("asset_token") val assetToken: String?
)

open class CommitReleaseUploadRequest(
    val status: String
)

open class CommitReleaseUploadResponse(
    @SerializedName("release_id") val releaseId: String,
    @SerializedName("release_url") val releaseUrl: String
)

open class PrepareSymbolUploadRequest(
    @SerializedName("symbol_type") val symbolType : String,
    @SerializedName("client_callback") val clientCallback : String? = null,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("build") val build : String? = null,
    @SerializedName("version") val version: String? = null
)

open class PrepareSymbolUploadResponse(
    @SerializedName("symbol_upload_id") val symbolUploadId : String,
    @SerializedName("upload_url") val uploadUrl : String,
    @SerializedName("expiration_date") val expirationDate: String? = null
)

open class CommitSymbolUploadRequest(
    val status: String
)

open class CommitSymbolUploadResponse(
    @SerializedName("symbol_upload_id") val symbolUploadId: String
)

open class DistributeRequest(
    @SerializedName("distribution_group_name") var distributionGroupName: String? = null,
    @SerializedName("distribution_group_id") var distributionGroupId: String? = null,
    @SerializedName("destination_name") var destinationName: String? = null,
    @SerializedName("destination_id") var destinationId: String? = null,
    @SerializedName("destination_type") var destinationType: String? = null,
    @SerializedName("release_notes") var releaseNotes: String? = null,
    @SerializedName("mandatory_update") var mandatoryUpdate: Boolean = false,
    @SerializedName("destinations") var destinations: List<Destination>? = null,
    @SerializedName("build") var build: Build? = null,
    @SerializedName("notify_testers") var notifyTesters: Boolean = false
) {

    open class Destination(
        @SerializedName("name") var name: String? = null,
        @SerializedName("id") var id: String? = null
    )

    open class Build(
        @SerializedName("branch") var branch: String? = null,
        @SerializedName("commit_hash") var commitHash: String? = null,
        @SerializedName("commit_message") var commitMessage: String? = null
    )

}


