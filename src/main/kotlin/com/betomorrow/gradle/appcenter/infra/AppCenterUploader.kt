package com.betomorrow.gradle.appcenter.infra

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

private const val RELEASE_URL_TEMPLATE =
    "https://appcenter.ms/orgs/%s/apps/%s/distribute/releases/%s"
private const val CONTENT_TYPE_APK = "application/vnd.android.package-archive"

// This leads to a maximum waiting time of about 15± minutes with a maximum total of 25 network requests (request time not included)
// The longest time between 2 requests would be:
private const val INITIAL_RETRY_DELAY = 1_000L
private const val MAX_RETRIES = 25
private const val BACKOFF_MULTIPLIER = 1.25

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
        var timeOutMs = INITIAL_RETRY_DELAY
        do {
            logger("Step 6/7 : Waiting for release to be ready to publish (${requestCount}s)")
            uploadResult = apiClient.getUpload(ownerName, appName, preparedUpload.id).executeOrThrow()
            Thread.sleep(timeOutMs)
            timeOutMs = (timeOutMs * BACKOFF_MULTIPLIER).toLong()

            if(uploadResult.uploadStatus == "error") {
                throw AppCenterUploaderException("Fetching release id failed, upload status equals 'error'.")
            }
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

        val uploadedReleaseId = uploadResult.releaseId
        apiClient.distribute(ownerName, appName, uploadedReleaseId!!, request).executeOrThrow()
        println("AppCenter release url is ${
            RELEASE_URL_TEMPLATE.format(ownerName, appName, uploadedReleaseId)
        }")
    }

    fun uploadMapping(mappingFile: File, versionName: String, versionCode: Int, logger: (String) -> Unit) {
        uploadSymbols(mappingFile, "AndroidProguard", versionName, versionCode.toString(), logger)
    }

    fun uploadSymbols(symbolsFile: File, versionName: String, versionCode: Int, logger: (String) -> Unit) {
        uploadSymbols(symbolsFile, "Breakpad", versionName, versionCode.toString(), logger)
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
