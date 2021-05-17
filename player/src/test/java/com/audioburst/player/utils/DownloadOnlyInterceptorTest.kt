package com.audioburst.player.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

internal class DownloadOnlyInterceptorTest {

    private fun testInterceptor(
        isUrlValid: Boolean,
        downloadTypeQueryParameter: String?,
    ) {
        // GIVEN
        val server = MockWebServer().apply {
            start()
            enqueue(MockResponse())
        }
        val url = "www.google.com"

        val interceptor = DownloadOnlyInterceptor(mediaUrlValidator = mediaUrlValidatorOf(isValid = isUrlValid))

        OkHttpClient().newBuilder()
            .addInterceptor(interceptor).build()
            .newCall(Request.Builder().url(server.url(url)).build()).execute()

        // WHEN
        val recordedRequest = server.takeRequest()

        // THEN
        assert(recordedRequest.requestUrl.queryParameter("downloadType") == downloadTypeQueryParameter)
        server.close()
    }

    @Test
    fun `test if downloadType is getting added to the url when validator returns true`() {
        testInterceptor(
            isUrlValid = true,
            downloadTypeQueryParameter = "1",
        )
    }

    @Test
    fun `test if downloadType is not getting added to the url when validator returns false`() {
        testInterceptor(
            isUrlValid = false,
            downloadTypeQueryParameter = null,
        )
    }
}