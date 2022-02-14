package com.audioburst.player.core.utils

import okhttp3.Interceptor
import okhttp3.Response

internal class DownloadOnlyInterceptor(
    private val mediaUrlValidator: MediaUrlValidator
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (!mediaUrlValidator.isValid(request.url.toString())) {
            return chain.proceed(request)
        }

        val url = request
            .url
            .newBuilder()
            .addQueryParameter(QUERY_NAME, DOWNLOAD_ONLY.toString())
            .build()

        request = request
            .newBuilder()
            .url(url)
            .build()

        return chain.proceed(request)
    }

    companion object {
        private const val QUERY_NAME: String = "downloadType"
        private const val DOWNLOAD_ONLY: Int = 1
    }
}