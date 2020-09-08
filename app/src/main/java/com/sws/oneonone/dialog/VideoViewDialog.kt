package com.sws.oneonone.dialog

import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.MediaController
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.sws.oneonone.R
import com.sws.oneonone.databinding.DialogViewerBinding
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage

class VideoViewDialog : DialogFragment() {
    var activity: BaseActivity? = null
    var binding: DialogViewerBinding? = null
    // for video player
    private val handler: Handler = Handler()
    private var cacheDataSourceFactory: CacheDataSourceFactory? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var simpleCache: SimpleCache? = null

    var type = ""
    var url = ""
    var thumbNail = ""

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.getWindow()!!.setLayout(width, height)
        }
    }

    companion object {

        fun newInstance(activity: BaseActivity, type :String?, url: String?, thumbNail: String?) = VideoViewDialog().apply {
            this.activity = activity
            this.type = type!!
            this.url = url!!
            this.thumbNail = thumbNail!!

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //this line is what you need:

        binding = DataBindingUtil.bind(inflater.inflate(R.layout.dialog_viewer, container, false))
        var dialog = dialog
        if (dialog != null) {
            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
  
        if (type.equals("image")) {
            binding!!.videoUrl?.visibility = View.GONE
            binding!!.ivView?.visibility = View.VISIBLE
            ShowingImage?.showBannerImage(
                activity!!,
                url,
                binding!!.ivView
            )
        } else {
            binding!!.ivThumbNail?.visibility = View.VISIBLE
            ShowingImage?.showBannerImage(activity!!, thumbNail, binding!!.ivThumbNail)

            Handler(Looper.getMainLooper()).postDelayed({
                //Do something after 100ms
                binding!!.ivThumbNail?.visibility = View.GONE
            }, 2500)

            binding!!.video?.visibility = View.VISIBLE
            //initPlayer(binding!!.video, url)
            playWithSttream(binding!!.video, url)
        }
        /*binding!!.tvMessage.text = message*/
        binding!!.ivClose.setOnClickListener {
            dismiss()
        }



    }

   /* private fun initPlayer(videoView: PlayerView, videoUrl: String) {
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

        val videoUri = Uri.parse(videoUrl)
        val mediaSource =
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(videoUri)

        videoView.player = simpleExoPlayer
        videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        simpleExoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        //handler.removeCallbacks(runnable)
                        //handler.postDelayed(runnable, 0)

                    }
                }
            }


        })
        simpleExoPlayer?.prepare(mediaSource, true, false)
    }*/

    fun playWithSttream(videoView: VideoView, url : String){
        val SrcPath = url//"http://akamedia2.lsops.net/live/smil:cnbc_en.smil/playlist.m3u8"
        //videoPlayer = VideoView(activity)
        videoView.setVideoURI(Uri.parse(SrcPath))
       // activity?.setContentView(videoView)
        videoView.setMediaController(MediaController(activity))
        videoView.requestFocus()
        videoView.start()
    }
}
