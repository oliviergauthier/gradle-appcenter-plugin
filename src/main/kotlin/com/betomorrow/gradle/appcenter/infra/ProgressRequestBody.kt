package com.betomorrow.gradle.appcenter.infra

import okhttp3.MediaType
import okio.Okio
import java.io.IOException
import okio.BufferedSink
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.Source
import java.io.File


class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val listener: (Long, Long) -> Unit
) : RequestBody() {

    private val contentLength: Long by lazy {
        file.length()
    }

    override fun contentLength(): Long {
        return contentLength
    }

    override fun contentType(): MediaType? {
        return MediaType.parse(contentType)
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        Okio.buffer(Okio.source(file)).use { source ->
            var total: Long = 0
            var read: Long = 0

            while ({ read = source.read(sink.buffer(), SEGMENT_SIZE); read }() != -1L) {
                total += read
                sink.emitCompleteSegments()
                this.listener(total, contentLength)
            }
        }
    }

    companion object {
        private const val SEGMENT_SIZE = 8192L // okio.Segment.SIZE
    }

}
