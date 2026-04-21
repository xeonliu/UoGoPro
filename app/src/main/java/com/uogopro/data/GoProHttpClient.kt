package com.uogopro.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoProHttpClient {
    suspend fun get(
        host: String,
        path: String,
        secure: Boolean = false,
        port: Int? = null,
    ): String = withContext(Dispatchers.IO) {
        val normalizedHost = normalizeHost(host, port)
        val protocol = if (secure) "https" else "http"
        val url = URL("$protocol://$normalizedHost$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 3_500
            readTimeout = 5_000
        }

        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = permissiveSslContext.socketFactory
            connection.hostnameVerifier = permissiveHostnameVerifier
        }

        try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            if (statusCode !in 200..299) {
                error("GoPro HTTP $statusCode: $body")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    fun url(host: String, path: String, secure: Boolean = false, port: Int? = null): String {
        val protocol = if (secure) "https" else "http"
        return "$protocol://${normalizeHost(host, port)}$path"
    }

    private fun normalizeHost(host: String, port: Int?): String {
        val withoutProtocol = host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
        return if (port == null) {
            withoutProtocol
        } else {
            "${withoutProtocol.substringBefore(':')}:$port"
        }
    }

    private val permissiveHostnameVerifier = HostnameVerifier { _, _ -> true }

    private val permissiveSslContext: SSLContext by lazy {
        val trustManagers = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            },
        )
        SSLContext.getInstance("TLS").apply {
            init(null, trustManagers, SecureRandom())
        }
    }
}
