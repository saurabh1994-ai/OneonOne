package com.sws.oneonone.fragment

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogPreviewBinding
import com.sws.oneonone.util.ApplicationLoader
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.BaseFragment

class PreviewFragment : BaseFragment(){
    var binding: DialogPreviewBinding? = null
    var videoUri : Uri? = null
    var image: Bitmap? = null
    var url = ""
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var simpleCache: SimpleCache? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity.window.statusBarColor =
            ContextCompat.getColor(activity, R.color.black)
        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_preview, container, false))

        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(videoUri != null){
            binding!!.video.setVideoURI(videoUri)
            binding!!.video.start()

        }

        if(image != null){
            binding!!.video.visibility = View.GONE
            binding!!.image.visibility = View.VISIBLE
            binding!!.videoUrl.visibility = View.GONE
            binding!!.image.setImageBitmap(image)
        }

        if(url.isNotEmpty()){
            binding!!.video.visibility = View.GONE
            binding!!.image.visibility = View.GONE
            binding!!.videoUrl.visibility = View.VISIBLE
            initPlayer()
        }

        binding!!.ivClose.setOnClickListener {
            onBackPressed()
        }



    }

    private fun initPlayer() {
        simpleExoPlayer = context?.let {
            SimpleExoPlayer.Builder(it).build()
        }

        simpleCache = ApplicationLoader.simpleCache

        cacheDataSourceFactory = CacheDataSourceFactory(
            simpleCache,
            DefaultHttpDataSourceFactory(context?.let {
                Util.getUserAgent(
                    it, getString(
                        R.string.app_name
                    )
                )
            }),
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )

        val videoUri = Uri.parse(url)
        val mediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

        binding!!.videoUrl.player = simpleExoPlayer
        binding!!.videoUrl.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        onBackPressed()

                    }
                }
            }


        })
        simpleExoPlayer?.prepare(mediaSource, true, false)
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    override fun onPause() {
        releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }


    companion object {


        fun newInstance(activity: BaseActivity, videoUri : Uri?, image: Bitmap?, url: String) = PreviewFragment().apply {

            this.activity = activity
            this.videoUri = videoUri
            this.image = image
            this.url = url

        }
    }
}