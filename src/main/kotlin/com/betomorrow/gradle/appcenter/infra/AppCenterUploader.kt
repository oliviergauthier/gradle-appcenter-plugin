package com.betomorrow.gradle.appcenter.infra

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

private const val CONTENT_TYPE_APK = "application/vnd.android.package-archive"

private const val RETRY_DELAY = 1_000L
private const val MAX_RETRIES = 60

class AppCenterUploader(
    private val apiFactory: AppCenterAPIFactory,
    private val ownerName: String,
    private val appName: String
) {

    fun uploadApk(
        file: File,
        changeLog: String,
        destinationNames: List<String>,
        notifyTesters: Boolean,
        logger: (String) -> Unit
    ) {
        logger("Step 1/7 : Prepare release upload")
        val apiClient = apiFactory.createApi()
        val preparedUpload = apiClient.prepareReleaseUpload(ownerName, appName).executeOrThrow()

        logger("Step 2/7 : Set metadata")
        val uploadApi = apiFactory.createUploadApi(preparedUpload.uploadDomain)
        val metadata = uploadApi.setMetadata(
            preparedUpload.packageAssetId,
            file.name,
            file.length(),
            preparedUpload.token,
            CONTENT_TYPE_APK
        ).executeOrThrow()

        logger("Step 3/7 : Upload release chunks")
        metadata.chunkList.forEachIndexed { i, chunkId ->
            val range = (i * metadata.chunkSize)..((i + 1) * metadata.chunkSize)
            val chunk = ChunkedRequestBody(file, range, "application/octet-stream")
            logger("Step 3/7 : Upload release chunk ${i + 1} of ${metadata.chunkList.size}")
            uploadApi.uploadChunk(
                preparedUpload.packageAssetId,
                chunkId,
                preparedUpload.token,
                chunk
            ).executeOrThrow()
        }

        logger("Step 4/7 : Finish upload")
        uploadApi.finishUpload(
            metadata.id,
            preparedUpload.token
        ).executeOrThrow()

        logger("Step 5/7 : Commit release")
        val commitRequest = CommitReleaseUploadRequest("uploadFinished")
        apiClient.commitReleaseUpload(ownerName, appName, preparedUpload.id, commitRequest).executeOrThrow()

        var requestCount = 0
        var uploadResult: GetUploadResponse?
        do {
            logger("Step 6/7 : Waiting for release to be ready to publish (${requestCount}s)")
            uploadResult = apiClient.getUpload(ownerName, appName, preparedUpload.id).executeOrThrow()
            Thread.sleep(RETRY_DELAY)

            if (++requestCount >= MAX_RETRIES) {
                throw AppCenterUploaderException("Fetching release id failed: Tried $requestCount times.")
            }
        } while (uploadResult?.uploadStatus != "readyToBePublished")

        logger("Step 7/7 : Distribute release")
        val request = DistributeRequest(
            destinations = destinationNames.map { DistributeRequest.Destination(it) }.toList(),
            releaseNotes = changeLog,
            notifyTesters = notifyTesters
        )

        apiClient.distribute(ownerName, appName, uploadResult.releaseId!!, request).executeOrThrow()
    }

    fun uploadSymbols(
        mappingFile: File,
        symbolType: String,
        versionName: String,
        versionCode: String,
        logger: (String) -> Unit
    ) {
        logger("Step 1/3 : Prepare symbol upload")
        val apiClient = apiFactory.createApi()
        val prepareRequest = PrepareSymbolUploadRequest(
            symbolType = symbolType,
            fileName = mappingFile.name,
            version = versionName,
            build = versionCode
        )
        val preparedUpload = apiClient.prepareSymbolUpload(ownerName, appName, prepareRequest).executeOrThrow()

        logger("Step 2/3 : Upload symbol")
        val api = apiFactory.createSymbolsApi()
        api.uploadSymbols(
            preparedUpload.uploadUrl,
            mappingFile.asRequestBody("text/plain; charset=UTF-8".toMediaTypeOrNull())
        ).executeOrThrow()

        logger("Step 3/3 : Commit symbol")
        val commitRequest = CommitSymbolUploadRequest("committed")
        apiClient.commitSymbolUpload(ownerName, appName, preparedUpload.symbolUploadId, commitRequest).executeOrThrow()
    }
}

private fun <T> Call<T>.executeOrThrow() = execute().bodyOrThrow()

private fun Call<Void>.executeOrThrow() = execute().successOrThrow()

private fun <T> Response<T>.bodyOrThrow() = successOrThrow()!!

private fun <T> Response<T>.successOrThrow() =
    if (isSuccessful) {
        body()
    } else {
        throw AppCenterUploaderException("Can't prepare release upload, code=${code()}, reason=${errorBody()?.string()}")
    }

class AppCenterUploaderException(message: String) : Exception(message)
