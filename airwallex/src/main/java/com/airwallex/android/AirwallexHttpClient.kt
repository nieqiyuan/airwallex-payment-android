package com.airwallex.android

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import java.io.IOException
import java.nio.charset.StandardCharsets

internal class AirwallexHttpClient(builder: OkHttpClient.Builder) {

    private val okHttpClient: OkHttpClient = builder.build()

    companion object {
        fun createClient(builder: OkHttpClient.Builder): AirwallexHttpClient {
            return AirwallexHttpClient(builder)
        }
    }

    @Throws(IOException::class)
    fun execute(parseRequest: AirwallexHttpRequest): AirwallexHttpResponse {
        val request = getRequest(parseRequest)
        val call = okHttpClient.newCall(request)
        val response = call.execute()
        return getResponse(response)
    }

    private fun getResponse(response: Response): AirwallexHttpResponse {
        // Status code
        val statusCode = response.code

        // Content
        val content = response.body?.byteStream()

        // Total size
        val totalSize = response.body?.contentLength() ?: 0

        // Reason phrase
        val reasonPhrase = response.message

        // Headers
        val headers: MutableMap<String, String?> = mutableMapOf()
        for (name in response.headers.names()) {
            headers[name] = response.header(name)
        }

        // Content type
        val contentType: String? = response.body?.contentType()?.toString()
        return AirwallexHttpResponse.Builder()
            .setStatusCode(statusCode)
            .setContent(content)
            .setTotalSize(totalSize)
            .setReasonPhrase(reasonPhrase)
            .setHeaders(headers)
            .setContentType(contentType)
            .build()
    }

    private fun getRequest(request: AirwallexHttpRequest): Request {
        val okHttpRequestBuilder = Request.Builder()
        val method: AirwallexHttpRequest.Method? = request.method
        // Set method
        if (method == AirwallexHttpRequest.Method.GET) {
            okHttpRequestBuilder.get()
        }

        // Set url
        okHttpRequestBuilder.url(request.url)

        // Set Header
        val okHttpHeadersBuilder = Headers.Builder()
        for ((key, value) in request.allHeaders) {
            okHttpHeadersBuilder.add(key, value)
        }

        // OkHttp automatically add gzip header so we do not need to deal with it
        val okHttpHeaders = okHttpHeadersBuilder.build()
        okHttpRequestBuilder.headers(okHttpHeaders)

        // Set Body
        val parseBody: AirwallexHttpBody? = request.body
        if (parseBody != null) {
            val okHttpRequestBody = AirwallexOkHttpRequestBody(parseBody)
            when (method) {
                AirwallexHttpRequest.Method.PUT -> okHttpRequestBuilder.put(okHttpRequestBody)
                AirwallexHttpRequest.Method.POST -> okHttpRequestBuilder.post(okHttpRequestBody)
                AirwallexHttpRequest.Method.DELETE -> okHttpRequestBuilder.delete(okHttpRequestBody)
            }
        }

        return okHttpRequestBuilder.build()
    }

    private class AirwallexOkHttpRequestBody internal constructor(val body: AirwallexHttpBody) :
        RequestBody() {
        private val content: ByteArray = body.content.toByteArray(StandardCharsets.UTF_8)
        private val offset = 0
        private val byteCount = content.size

        override fun contentLength(): Long {
            return byteCount.toLong()
        }

        override fun contentType(): MediaType? {
            return body.contentType.toMediaTypeOrNull()
        }

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            sink.write(content, offset, byteCount)
        }
    }

}