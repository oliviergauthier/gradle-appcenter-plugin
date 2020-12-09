package com.betomorrow.gradle.appcenter.infra

import okhttp3.*
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AppCenterUploader(
    private val apiClient: AppCenterAPI,
    private val okHttpClient: OkHttpClient,
    private val ownerName: String,
    private val appName: String
) {

    fun uploadApk(file: File, changeLog: String, destinationNames: List<String>, notifyTesters: Boolean, logger: (String) -> Unit): String {
        logger("Starting release upload...")
        val prepareResponse = apiClient.prepareReleaseUpload(ownerName, appName).execute()
        if (!prepareResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't prepare release upload, code=${prepareResponse.code()}, " +
                        "reason=${prepareResponse.errorBody()?.string()}"
            )
        }

        val preparedUpload = prepareResponse.body()!!

        logger("Setting Metadata...")
        val chunkSizeResponse = getChunkSize(preparedUpload, file).execute()
        if (!chunkSizeResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't set metadata, code=${chunkSizeResponse.code()}, " +
                        "reason=${chunkSizeResponse.errorBody()?.string()}"
            )
        }

        logger("Uploading release binary chunks...")
        uploadApkChunks(preparedUpload, file, chunkSizeResponse.body()!!.chunkSize, logger)

        logger("Finishing release...")
        val finishReleaseResponse = finishRelease(preparedUpload).execute()
        if(!finishReleaseResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't finish release, code=${finishReleaseResponse.code()}, " +
                        "reason=${finishReleaseResponse.errorBody()?.string()}"
            )
        }

        logger("Committing release...")
        val commitRequest = CommitReleaseUploadRequest("uploadFinished", preparedUpload.uploadId)
        val commitResponse = apiClient.commitReleaseUpload(ownerName, appName, preparedUpload.uploadId, commitRequest).execute()
        if (!commitResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't commit release, code=${commitResponse.code()}, " +
                        "reason=${commitResponse.errorBody()?.string()}"
            )
        }

        logger("Waiting for release to be ready...")
        val releaseDistinctId = waitForReadyRelease(preparedUpload, logger)

        logger("Distributing Release...")
        val request = DistributeRequest(
            destinations = destinationNames.map { DistributeRequest.Destination(it) }.toList(),
            releaseNotes = changeLog,
            notifyTesters = notifyTesters
        )

        val distributeResponse = apiClient.distribute(ownerName, appName, releaseDistinctId, request).execute()
        if (!distributeResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't distribute release, code=${distributeResponse.code()}, " +
                        "reason=${distributeResponse.errorBody()?.string()}"
            )
        }

        return releaseDistinctId
    }

    private fun waitForReadyRelease(preparedUpload: PrepareReleaseUploadResponse, logger: (String) -> Unit): String {
        val maxRetryCount = 50
        var retryCount = 0
        while(true) {
            val statusResponse = apiClient.getUploadStatus(ownerName, appName, preparedUpload.uploadId).execute()
            if(!statusResponse.isSuccessful) {
                throw AppCenterUploaderException(
                    "Can't get upload status, code=${statusResponse.code()}, " +
                            "reason=${statusResponse.errorBody()?.string()}"
                )
            } else if(statusResponse.body()?.status == "readyToBePublished") {
                return statusResponse.body()!!.releaseDistinctId
            }
            if(++retryCount > maxRetryCount) {
                throw AppCenterUploaderException(
                    "Can't get upload status, code=${statusResponse.code()}, " +
                            "reason=${statusResponse.errorBody()?.string()}"
                )
            }
            logger("Upload status is ${statusResponse.body()?.status}. Expected to be `readyToBePublished`. Retry $retryCount/$maxRetryCount in 1 minute...")
            Thread.sleep(TimeUnit.MINUTES.toMillis(1))
        }
    }

    private fun finishRelease(preparedUpload: PrepareReleaseUploadResponse): retrofit2.Call<Unit> {
        val finishUrl = "${preparedUpload.uploadDomain}/upload/finished/${preparedUpload.packageAssetId}?token=${preparedUpload.urlEncodedToken}"
        return apiClient.finishRelease(finishUrl)
    }

    fun uploadSymbols(mappingFile: File, versionName: String, versionCode : String, logger: (String) -> Unit) {
        logger("Step 1/3 : Prepare Symbol")
        val prepareRequest = PrepareSymbolUploadRequest(
            symbolType = "AndroidProguard",
            fileName = mappingFile.name,
            version = versionName,
            build = versionCode
        )
        val prepareResponse = apiClient.prepareSymbolUpload(ownerName, appName, prepareRequest).execute()
        if (!prepareResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't prepare symbol upload, code=${prepareResponse.code()}, " +
                        "reason=${prepareResponse.errorBody()?.string()}"
            )
        }

        val preparedUpload = prepareResponse.body()!!

        logger("Step 2/3 : Upload Symbol")
        val uploadResponse = doUploadSymbol(preparedUpload.uploadUrl, mappingFile, logger).execute()
        if (!uploadResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't upload mapping, code=${uploadResponse.code()}, " +
                        "reason=${uploadResponse.body()?.string()}"
            )
        }

        logger("Step 3/3 : Commit Symbol")
        val commitRequest = CommitSymbolUploadRequest("committed")
        val commitResponse = apiClient.commitSymbolUpload(ownerName, appName, preparedUpload.symbolUploadId, commitRequest).execute()
        if (!commitResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't commit symbol, code=${commitResponse.code()}, " +
                        "reason=${commitResponse.errorBody()?.string()}"
            )
        }
    }

    private fun uploadApkChunks(uploadResponse: PrepareReleaseUploadResponse, file: File, chunkSize: Int, logger: (String) -> Unit) {
        var blockNumber = 1

        val uploadUrl = "${uploadResponse.uploadDomain}/upload/upload_chunk/${uploadResponse.packageAssetId}" +
                "?token=${uploadResponse.urlEncodedToken}"

        val chunked = file.readBytes().asIterable().chunked(chunkSize)
        chunked.forEach { chunk ->

            val uploadChunkUrl = "${uploadUrl}&block_number=$blockNumber"

            val body = MultipartBody.create(
                MediaType.parse("application/octet-stream"),
                chunk.toByteArray()
            )

            val request = Request.Builder()
                .url(uploadChunkUrl)
                .post(body)
                .build()

           val chunkResponse = okHttpClient.newCall(request).execute()

            if (!chunkResponse.isSuccessful) {
                throw AppCenterUploaderException(
                    "Can't upload chunk $blockNumber, code=${chunkResponse.code()}, " +
                            "reason=${chunkResponse.body()?.string()}"
                )
            } else {
                logger("Chunk $blockNumber/${chunked.size} was uploaded.")
            }
            blockNumber++
        }
    }

    private fun getChunkSize(preparedUpload: PrepareReleaseUploadResponse, file: File): retrofit2.Call<SetMetadataResponse> {
        val metadataUrl = "${preparedUpload.uploadDomain}/upload/set_metadata/${preparedUpload.packageAssetId}" +
                "?file_name=${URLEncoder.encode(file.name, "utf-8")}" +
                "&file_size=${file.length()}" +
                "&token=${preparedUpload.urlEncodedToken}" +
                "&content_type=application%2Fvnd.android.package-archive"
        return apiClient.setMetadata(metadataUrl)
    }

    private fun doUploadSymbol(uploadUrl: String, file: File, logger: (String) -> Unit): Call {
        val request = Request.Builder()
            .url(uploadUrl)
            .addHeader("x-ms-blob-type", "BlockBlob")
            .put(RequestBody.create(MediaType.parse("text/plain; charset=UTF-8"), file))
            .build()

        return okHttpClient.newCall(request)
    }



}

class AppCenterUploaderException(message: String) : Exception(message)