package com.audioburst.player.di

import android.content.Context
import com.audioburst.player.di.provider.Provider
import com.audioburst.player.di.provider.singleton
import com.audioburst.player.media.AdUriResolver
import com.audioburst.player.utils.DownloadOnlyInterceptor
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.extractor.Extractor
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.ResolvingDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors

internal class MediaSourceModule(
    private val context: Context,
    private val downloadOnlyInterceptorProvider: Provider<DownloadOnlyInterceptor>,
    private val adUriResolverProvider: Provider<AdUriResolver>,
) {

    private val userAgentProvider: Provider<String> =
        singleton { Util.getUserAgent(context, COMPONENT_NAME) }

    private val exoPlayerDatabaseProvider: Provider<DatabaseProvider> =
        singleton { ExoDatabaseProvider(context) }

    private val downloadCacheProvider: Provider<Cache> = singleton {
        val cacheFile = context.getExternalFilesDir(null) ?: context.filesDir
        SimpleCache(
            File(cacheFile, DOWNLOAD_CONTENT_DIRECTORY),
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES),
            exoPlayerDatabaseProvider.get()
        )
    }

    private val okHttpClientProvider: Provider<OkHttpClient> = singleton {
        OkHttpClient.Builder()
            .addInterceptor(downloadOnlyInterceptorProvider.get())
            .build()
    }

    val mediaSourceFactoryProvider: Provider<MediaSourceFactory> = singleton {
        val extractorFactory = ExtractorsFactory {
            arrayOf<Extractor>(Mp3Extractor())
        }

        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClientProvider.get())
            .setUserAgent(userAgentProvider.get())

        val dataSourceFactory = ResolvingDataSource.Factory(httpDataSourceFactory, adUriResolverProvider.get())

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(downloadCacheProvider.get())
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        DefaultMediaSourceFactory(cacheDataSourceFactory, extractorFactory)
    }

    val downloadManagerProvider: Provider<DownloadManager> = singleton {
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClientProvider.get())
            .setUserAgent(userAgentProvider.get())

        val parallelDownloads = PARALLEL_DOWNLOADS

        DownloadManager(
            context,
            exoPlayerDatabaseProvider.get(),
            downloadCacheProvider.get(),
            httpDataSourceFactory,
            Executors.newFixedThreadPool(parallelDownloads)
        ).apply {
            maxParallelDownloads = parallelDownloads
        }
    }

    companion object {
        private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        private const val CACHE_SIZE_BYTES: Long = 1024 * 1024 * 200
        private const val COMPONENT_NAME = "AudioburstSDK"
        private const val PARALLEL_DOWNLOADS = 3
    }
}