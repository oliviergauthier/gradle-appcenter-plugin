package com.betomorrow.gradle.appcenter.infra

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.buffer
import okio.source
import java.io.File
import java.io.IOException

class ChunkedRequestBody(
    private val file: File,
    private val range: LongRange,
    private val contentType: String
) : RequestBody() {

    private val contentLength: Long by lazy {
        (range.last - range.first).coerceAtMost(file.length() - range.first)
    }

    override fun contentLength(): Long = contentLength

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        file.source().buffer().use { source ->
            // Skip until beginning of chunk
            source.skip(range.first)

            // Read until end of chunk
            var toRead = contentLength
            while (toRead > 0) {
                val read = source.read(sink.buffer, toRead)
                if (read >= 0) {
                    toRead -= read
                } else {
                    break
                }
            }
        }
    }
}
