package com.betomorrow.gradle.appcenter.infra

import okhttp3.*
import java.io.File

class AppCenterUploader(
    val apiClient: AppCenterAPI,
    val okHttpClient: OkHttpClient,
    val ownerName: String,
    val appName: String
) {

    fun upload(file: File, changeLog: String, destinationNames: List<String>) {
        val prepareResponse = apiClient.prepare(ownerName, appName).execute()
        if (!prepareResponse.isSuccessful) {
            throw AppCenterUploaderException("Can't prepare release, code=${prepareResponse.code()}, " +
                    "reason=${prepareResponse.errorBody()?.string()}")
        }

        val preparedUpload = prepareResponse.body()!!

        val uploadResponse = doUpload(preparedUpload.uploadUrl, file).execute()
        if (!uploadResponse.isSuccessful) {
            throw AppCenterUploaderException("Can't upload APK, code=${uploadResponse.code()}, " +
                    "reason=${uploadResponse.body()?.string()}")
        }

        val commitRequest = CommitRequest("committed")
        val commitResponse = apiClient.commit(ownerName, appName, preparedUpload.uploadId, commitRequest).execute()
        if (!commitResponse.isSuccessful) {
            throw AppCenterUploaderException("Can't commit release, code=${commitResponse.code()}, " +
                    "reason=${commitResponse.errorBody()?.string()}")
        }

        val committed = commitResponse.body()!!
        val request = DistributeRequest()
        request.destinations = destinationNames.map { DistributeRequest.Destination(it) }.toList()
        request.releaseNotes = changeLog

        val distributeResponse = apiClient.distribute(ownerName, appName, committed.releaseId, request).execute()
        if (!distributeResponse.isSuccessful) {
            throw AppCenterUploaderException("Can't distribute release, code=${distributeResponse.code()}, " +
                    "reason=${distributeResponse.errorBody()?.string()}")
        }
    }

    private fun doUpload(uploadUrl: String, file: File) : Call {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "ipa", file.name,
                RequestBody.create(MediaType.parse("application/octet-stream"), file)
            )
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(body)
            .build()

        return okHttpClient.newCall(request)
    }

}

class AppCenterUploaderException(message: String) : Exception(message) {

}