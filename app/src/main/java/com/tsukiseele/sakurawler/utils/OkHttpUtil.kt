package com.tsukiseele.sakurawler.utils

import java.io.IOException
import java.net.SocketException
import java.util.concurrent.TimeUnit

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OkHttpUtil private constructor(private val client: OkHttpClient) {
    companion object {
        private val TIMEOUT = 15
        private var CLIENT: OkHttpUtil? = null

        fun getClient(): OkHttpClient {
            return instance().client
        }

        fun instance(): OkHttpUtil {
            if (CLIENT == null) {
                synchronized(OkHttpUtil::class.java) {
                    CLIENT = OkHttpUtil(
                        OkHttpClient.Builder()
                            .connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                            .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                            .build()
                    )
                }
            }
            return CLIENT!!
        }

        @Throws(IOException::class)
        @JvmOverloads
        operator fun get(url: String, headers: Map<String, String>? = null): Response {
            val requestBuilder = Request.Builder()
            requestBuilder.url(url)
            if (headers != null)
                requestBuilder.headers(Headers.of(headers))
            val res = getClient().newCall(requestBuilder.build()).execute()
            if (res.isSuccessful)
                return res
            throw SocketException("Failed: " + res.message())
        }
    }
}
