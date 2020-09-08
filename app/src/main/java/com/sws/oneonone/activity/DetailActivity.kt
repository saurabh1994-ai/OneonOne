package com.sws.oneonone.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.igreenwood.loupe.Loupe
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import com.sws.oneonone.R
import com.sws.oneonone.databinding.FragmentDetailBinding
import com.sws.oneonone.databinding.ItemImageBinding
import com.sws.oneonone.util.Pref
import kotlinx.android.synthetic.main.fragment_detail.*


class DetailActivity : AppCompatActivity() {

    companion object {
        private const val ARGS_IMAGE_URLS = "ARGS_IMAGE_URLS"
        private const val ARGS_INITIAL_POSITION = "ARGS_INITIAL_POSITION"

        fun createIntent(context: Context, urls: ArrayList<String>, initialPos: Int): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(ARGS_IMAGE_URLS, urls)
                putExtra(ARGS_INITIAL_POSITION, initialPos)
            }
        }
    }

    private lateinit var binding: FragmentDetailBinding

    @Suppress("UNCHECKED_CAST")
    private val urls: List<String> by lazy { intent.getSerializableExtra(ARGS_IMAGE_URLS) as List<String> }
    private val initialPos: Int by lazy { intent.getIntExtra(ARGS_INITIAL_POSITION, 0) }
    private var adapter: ImageAdapter? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Pref.useSharedElements) {
            postponeEnterTransition()
        }

        binding = DataBindingUtil.setContentView(this, R.layout.fragment_detail)

        //android O fix orientation bug
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        initToolbar()
        initViewPager()
    }

    override fun onBackPressed() {
        adapter?.clear()
        super.onBackPressed()
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    private fun initViewPager() {
        adapter = ImageAdapter(this, urls)
        binding.viewpager.adapter = adapter
        binding.viewpager.currentItem = initialPos
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = ""
        }
    }

    private fun showToolbar() {
        binding.toolbar.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(0f)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (Pref.useSharedElements) {
            supportFinishAfterTransition()
        } else {
            finish()
        }
        return true
    }

    override fun finish() {
        super.finish()
        if (!Pref.useSharedElements) {
            overridePendingTransition(0, R.anim.fade_out_fast)
        }
    }

    private fun hideToolbar() {
        binding.toolbar.animate()
            .setInterpolator(AccelerateDecelerateInterpolator())
            .translationY(-binding.toolbar.height.toFloat())
    }

    inner class ImageAdapter(var context: Context, var urls: List<String>) : PagerAdapter() {

        private var loupeMap = hashMapOf<Int, Loupe>()
        private var views = hashMapOf<Int, ImageView>()
        private var currentPos = 0

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val binding = ItemImageBinding.inflate(LayoutInflater.from(context))
            container.addView(binding.root)
            loadImage(binding.image, binding.container, position)
            views[position] = binding.image
            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            super.setPrimaryItem(container, position, obj)
            this.currentPos = position
        }

        override fun getCount() = urls.size

        private fun loadImage(image: ImageView, container: ViewGroup, position: Int) {
            if (Pref.useSharedElements) {
                // shared elements
                Glide.with(image.context)
                    .load(urls[position])
                    .onlyRetrieveFromCache(true)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            startPostponedEnterTransition()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            image.transitionName =
                                context.getString(R.string.shared_image_transition, position)

                            val loupe = createLoupe(image, container) {
                                useFlingToDismissGesture = !Pref.useSharedElements
                                maxZoom = Pref.maxZoom
                                flingAnimationDuration = Pref.flingAnimationDuration
                                scaleAnimationDuration = Pref.scaleAnimationDuration
                                overScaleAnimationDuration = Pref.overScaleAnimationDuration
                                overScrollAnimationDuration = Pref.overScrollAnimationDuration
                                dismissAnimationDuration = Pref.dismissAnimationDuration
                                restoreAnimationDuration = Pref.restoreAnimationDuration
                                viewDragFriction = Pref.viewDragFriction

                                setOnViewTranslateListener(
                                    onStart = { hideToolbar() },
                                    onRestore = { showToolbar() },
                                    onDismiss = { finishAfterTransition() }
                                )
                            }

                            loupeMap[position] = loupe

                            if (position == initialPos) {
                                setEnterSharedElementCallback(object : SharedElementCallback() {
                                    override fun onMapSharedElements(
                                        names: MutableList<String>?,
                                        sharedElements: MutableMap<String, View>?
                                    ) {
                                        names ?: return
                                        val view = views[viewpager.currentItem] ?: return
                                        val currentPosition: Int = viewpager.currentItem
                                        view.transitionName = context.getString(
                                            R.string.shared_image_transition,
                                            currentPosition
                                        )
                                        sharedElements?.clear()
                                        sharedElements?.put(view.transitionName, view)
                                    }
                                })
                                startPostponedEnterTransition()
                            }
                            return false
                        }

                    })
                    .into(image)
            } else {
                // swipe to dismiss
                Glide.with(image.context).load(urls[position])
                    .onlyRetrieveFromCache(true)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            startPostponedEnterTransition()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val loupe = createLoupe(image, container) {
                                useFlingToDismissGesture = !Pref.useSharedElements
                                maxZoom = Pref.maxZoom
                                flingAnimationDuration = Pref.flingAnimationDuration
                                scaleAnimationDuration = Pref.scaleAnimationDuration
                                overScaleAnimationDuration = Pref.overScaleAnimationDuration
                                overScrollAnimationDuration = Pref.overScrollAnimationDuration
                                dismissAnimationDuration = Pref.dismissAnimationDuration
                                restoreAnimationDuration = Pref.restoreAnimationDuration
                                viewDragFriction = Pref.viewDragFriction

                                setOnViewTranslateListener(
                                    onStart = { hideToolbar() },
                                    onRestore = { showToolbar() },
                                    onDismiss = { finish() }
                                )
                            }

                            loupeMap[position] = loupe
                            if (position == initialPos) {
                                startPostponedEnterTransition()
                            }
                            return false
                        }

                    }).into(image)
            }
        }

        fun clear() {
            // clear refs
            loupeMap.forEach {
                val loupe = it.value
                // clear refs
                loupe.cleanup()
            }
            loupeMap.clear()
        }
    }
}
