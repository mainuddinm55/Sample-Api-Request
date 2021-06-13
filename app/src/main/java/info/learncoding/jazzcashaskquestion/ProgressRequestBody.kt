package info.learncoding.jazzcashaskquestion

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.*
import java.io.IOException

class ProgressRequestBody(
    private val multipartBody: MultipartBody,
    private val listener: (progress: Int) -> Unit
) : RequestBody() {
    private lateinit var countingSink: CountingSink
    override fun contentType(): MediaType? {
        return multipartBody.contentType()
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return multipartBody.contentLength()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        countingSink = CountingSink(sink, multipartBody, listener)
        val bufferedSink = Okio.buffer(countingSink)
        multipartBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    inner class CountingSink(
        delegate: Sink,
        private val multipartBody: MultipartBody,
        private val listener: (progress: Int) -> Unit
    ) : ForwardingSink(delegate) {
        private var byteWritten = 0L
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            byteWritten += byteCount
            val progress = (100F * byteWritten / multipartBody.contentLength()).toInt()
            listener.invoke(progress)
        }
    }
}