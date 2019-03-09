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

    private val contentLength : Long by lazy {
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
        var source: Source? = null
        try {
            source = Okio.source(file)
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
            Util.closeQuietly(source)
        }
    }

    companion object {
        private val SEGMENT_SIZE : Long = 2048 // okio.Segment.SIZE
    }

}