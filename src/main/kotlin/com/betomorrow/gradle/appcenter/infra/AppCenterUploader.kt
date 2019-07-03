package com.betomorrow.gradle.appcenter.infra

import okhttp3.*
import org.gradle.internal.logging.progress.ProgressLogger
import java.io.File
import kotlin.math.log

class AppCenterUploader(
    val apiClient: AppCenterAPI,
    val okHttpClient: OkHttpClient,
    val ownerName: String,
    val appName: String
) {

    fun upload(file: File, changeLog: String, destinationNames: List<String>, notifyTesters: Boolean) {
        upload(file, changeLog, destinationNames, notifyTesters) { }
    }

    fun upload(file: File, changeLog: String, destinationNames: List<String>, notifyTesters: Boolean, logger: (String) -> Unit) {
        logger("Step 1/4 : Prepare Release")
        val prepareResponse = apiClient.prepare(ownerName, appName).execute()
        if (!prepareResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't prepare release, code=${prepareResponse.code()}, " +
                        "reason=${prepareResponse.errorBody()?.string()}"
            )
        }

        val preparedUpload = prepareResponse.body()!!

        logger("Step 2/4 : Upload file")
        val uploadResponse = doUpload(preparedUpload.uploadUrl, file, logger).execute()
        if (!uploadResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't upload APK, code=${uploadResponse.code()}, " +
                        "reason=${uploadResponse.body()?.string()}"
            )
        }

        logger("Step 3/4 : Commit release")
        val commitRequest = CommitRequest("committed")
        val commitResponse = apiClient.commit(ownerName, appName, preparedUpload.uploadId, commitRequest).execute()
        if (!commitResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't commit release, code=${commitResponse.code()}, " +
                        "reason=${commitResponse.errorBody()?.string()}"
            )
        }

        logger("Step 4/4 : Distribute Release")
        val committed = commitResponse.body()!!
        val request = DistributeRequest()
        request.destinations = destinationNames.map { DistributeRequest.Destination(it) }.toList()
        request.releaseNotes = changeLog
        request.notifyTesters = notifyTesters

        val distributeResponse = apiClient.distribute(ownerName, appName, committed.releaseId, request).execute()
        if (!distributeResponse.isSuccessful) {
            throw AppCenterUploaderException(
                "Can't distribute release, code=${distributeResponse.code()}, " +
                        "reason=${distributeResponse.errorBody()?.string()}"
            )
        }
    }

    private fun doUpload(uploadUrl: String, file: File, logger: (String) -> Unit): Call {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "ipa", file.name,
                ProgressRequestBody(file, "application/octet-stream") { current, total ->
                    //                    print("Current $current / $total")
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

}

class AppCenterUploaderException(message: String) : Exception(message) {

}