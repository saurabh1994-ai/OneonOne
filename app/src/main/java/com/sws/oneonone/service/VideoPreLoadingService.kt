package com.sws.oneonone.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.sws.oneonone.R
import com.sws.oneonone.util.ApplicationLoader
import com.sws.oneonone.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideoPreLoadingService :
    IntentService(VideoPreLoadingService::class.java.simpleName) {
    private lateinit var mContext: Context
    private var simpleCache: SimpleCache? = null
    private var cachingJob: Job? = null
    private var videosList: ArrayList<String>? = null
    private var videoUrl : String? = null

    override fun onHandleIntent(intent: Intent?) {
        mContext = applicationContext
        simpleCache = ApplicationLoader.simpleCache

        if (intent != null) {
            val extras = intent.extras
            videoUrl = extras?.getString(Constants.VIDEO_URL)

            if (!videoUrl.isNullOrEmpty()) {
                preCacheVideo(videoUrl)
            }
        }
    }

    private fun preCacheVideo(videosList: String?) {
        var videoUrl: String? = null
        if (!videosList.isNullOrEmpty()) {
            videoUrl = videosList
           // videosList.removeAt(0)
        } else {
            stopSelf()
        }
        if (!videoUrl.isNullOrBlank()) {
            val videoUri = Uri.parse(videoUrl)
            val dataSpec = DataSpec(videoUri)
            val defaultCacheKeyFactory = CacheUtil.DEFAULT_CACHE_KEY_FACTORY
            val progressListener =
                CacheUtil.ProgressListener { requestLength, bytesCached, newBytesCached ->
                    val downloadPercentage: Double = (bytesCached * 100.0
                            / requestLength)
                }
            val dataSource: DataSource =
                    DefaultDataSourceFactory(
                            mContext,
                            Util.getUserAgent(this, getString(R.string.app_name))).createDataSource()

            cachingJob = GlobalScope.launch(Dispatchers.IO) {
                cacheVideo(dataSpec, defaultCacheKeyFactory, dataSource, progressListener)
                preCacheVideo(videosList)
            }
        }
    }

    private fun cacheVideo(
        dataSpec: DataSpec,
        defaultCacheKeyFactory: CacheKeyFactory?,
        dataSource: DataSource,
        progressListener: CacheUtil.ProgressListener
    ) {
        CacheUtil.cache(
            dataSpec,
            simpleCache,
            defaultCacheKeyFactory,
            dataSource,
            progressListener,
            null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cachingJob?.cancel()
    }
}