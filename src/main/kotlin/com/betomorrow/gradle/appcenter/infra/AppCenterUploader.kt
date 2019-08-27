package com.betomorrow.gradle.appcenter.infra

import okhttp3.*
import java.io.File

class AppCenterUploader(
    private val apiClient: AppCenterAPI,
    private val okHttpClient: OkHttpClient,
    private val ownerName: String,
    private val appName: String
) {

    fun uploadApk(file: File, changeLog: String, destinationNames: List<String>, notifyTesters: Boolean) {
        uploadApk(file, changeLog, destinationNames, notifyTesters) { }
    }

    fun uploadApk(file: File, changeLog: String, destinationNames: List<String>, notifyTesters: Boolean, logger: (String) -> Unit) {
        logger("Step 1/4 : Prepare Release Upload")
        val prepareResponse = apiClient.prepareReleaseUpload(ownerName, appName).execute()
        if (!prepareResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't prepare release upload, code=${prepareResponse.code()}, " +
                        "reason=${prepareResponse.errorBody()?.string()}"
            )
        }

        val preparedUpload = prepareResponse.body()!!

        logger("Step 2/4 : Upload Release file")
        val uploadResponse = doUploadApk(preparedUpload.uploadUrl, file, logger).execute()
        if (!uploadResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't upload APK, code=${uploadResponse.code()}, " +
                        "reason=${uploadResponse.body()?.string()}"
            )
        }

        logger("Step 3/4 : Commit release")
        val commitRequest = CommitReleaseUploadRequest("committed")
        val commitResponse = apiClient.commitReleaseUpload(ownerName, appName, preparedUpload.uploadId, commitRequest).execute()
        if (!commitResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't commit release, code=${commitResponse.code()}, " +
                        "reason=${commitResponse.errorBody()?.string()}"
            )
        }

        logger("Step 4/4 : Distribute Release")
        val committed = commitResponse.body()!!
        val request = DistributeRequest(
            destinations = destinationNames.map { DistributeRequest.Destination(it) }.toList(),
            releaseNotes = changeLog,
            notifyTesters = notifyTesters
        )

        val distributeResponse = apiClient.distribute(ownerName, appName, committed.releaseId, request).execute()
        if (!distributeResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't distribute release, code=${distributeResponse.code()}, " +
                        "reason=${distributeResponse.errorBody()?.string()}"
            )
        }
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

    private fun doUploadApk(uploadUrl: String, file: File, logger: (String) -> Unit): Call {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "ipa", file.name,
                ProgressRequestBody(file, "application/octet-stream") { current, total ->
                    val progress : Int = ((current.toDouble() / total) * 100).toInt()
                    logger("Step 2/4 : Upload apk ($progress %)")
                }
            )
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(body)
            .build()

        return okHttpClient.newCall(request)
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