package com.betomorrow.gradle.appcenter.infra

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.File
import java.io.IOException


class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val listener: (Long, Long) -> Unit
) : RequestBody() {

    private val contentLength : Long by lazy {
        file.length()
    }

    override fun contentLength(): Long {
        return contentLength
    }

    override fun contentType(): MediaType? {
        return contentType.toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null
        try {
            source = file.source()
            if (source == null) {
                return
            }

            var total: Long = 0
            var read: Long = 0

            while ({read = source.read(sink.buffer(),
                    SEGMENT_SIZE
                ); read}() != -1L) {
                total += read
                sink.flush()
                this.listener(total, contentLength)
            }
        } finally {
            // Copied from the okhttp3.internal.Util (version 3.12.x) because
            // you shouldn't be using this internal namespace
            if (source != null) {
                try {
                    source.close()
                } catch (rethrown: RuntimeException) {
                    throw rethrown
                } catch (ignored: Exception) {
                }
            }
        }
    }

    companion object {
        private val SEGMENT_SIZE : Long = 2048 // okio.Segment.SIZE
    }

}