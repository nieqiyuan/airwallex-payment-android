package com.airwallex.android

import java.io.UnsupportedEncodingException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

internal class ConnectionFactory {
    @JvmSynthetic
    internal fun create(request: AirwallexRequest): AirwallexConnection {
        // HttpURLConnection verifies SSL cert by default
        val conn = openConnection(request.baseUrl).apply {
            connectTimeout = CONNECT_TIMEOUT
            readTimeout = READ_TIMEOUT
            useCaches = false
            sslSocketFactory = SSL_SOCKET_FACTORY
            requestMethod = request.method.code

//            for ((key, value) in request.headers) {
//                setRequestProperty(key, value)
//            }

            if (AirwallexRequest.Method.POST == request.method) {
                doOutput = true
//                setRequestProperty("Content-Type", request.contentType)
                outputStream.use { output -> output.write(getRequestOutputBytes(request)) }
            }
        }

        return AirwallexConnection(conn)
    }

    private fun openConnection(requestUrl: String): HttpsURLConnection {
        return URL(requestUrl).openConnection() as HttpsURLConnection
    }

    @JvmSynthetic
    internal fun getRequestOutputBytes(request: AirwallexRequest): ByteArray {

        return request.getOutputBytes()
//        try {
//            return request.getOutputBytes()
//        } catch (e: UnsupportedEncodingException) {
//            throw InvalidRequestException(
//                "Unable to encode parameters to ${StandardCharsets.UTF_8.name()}. " +
//                    "Please contact support@stripe.com for assistance.",
//                null, null, 0, null, null, null, e
//            )
//        }
    }

    private companion object {
        private val SSL_SOCKET_FACTORY = AirwallexSSLSocketFactory()
        private val CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(30).toInt()
        private val READ_TIMEOUT = TimeUnit.SECONDS.toMillis(80).toInt()
    }
}