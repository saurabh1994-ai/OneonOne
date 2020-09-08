package com.sws.oneonone.util

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class ApplicationLoader : MultiDexApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        adjustFontScale(getResources().getConfiguration());
        Companion.applicationContext = applicationContext
        applicationHandler =
            Handler(applicationContext.mainLooper)

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        }

        if (exoDatabaseProvider != null) {
            exoDatabaseProvider = ExoDatabaseProvider(this)
        }

        if (simpleCache == null) {
            simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)
        }

    }

    companion object {
        var REQUEST_TAG = "global"

        @Volatile
        var applicationContext: Context? = null

        @JvmField
        @Volatile
        var applicationHandler: Handler? = null

        var simpleCache: SimpleCache? = null
        var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        var exoDatabaseProvider: ExoDatabaseProvider? = null
        var exoPlayerCacheSize: Long = 90 * 1024 * 1024
    }

    fun adjustFontScale(configuration: Configuration) {
        configuration.fontScale = 1.0.toFloat()
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm: WindowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.getDefaultDisplay().getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        baseContext.resources.updateConfiguration(configuration, metrics)
    }
}