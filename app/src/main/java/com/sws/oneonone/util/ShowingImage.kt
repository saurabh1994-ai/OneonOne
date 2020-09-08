package com.sws.oneonone.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView
import com.sws.oneonone.R
import de.hdodenhof.circleimageview.CircleImageView

class ShowingImage  {
    companion object {

        fun showImage(activity: BaseActivity, url: String?, imageView: CircleImageView){
            if (!url.isNullOrBlank()){
                Glide.with(activity)
                    .load(url)
                     .placeholder(R.drawable.avtar_icon)
                     .error(R.drawable.avtar_icon)
                    .into(imageView)
            }
        }

        fun showBannerImage(activity: BaseActivity, url: String?, imageView: ImageView){
            if (!url.isNullOrBlank()){
                Glide.with(activity)
                    .load(url)
                    //  .placeholder(R.drawable.profile)
                    //  .error(R.drawable.profile)
                    .into(imageView)
            }
        }

        fun showChatImage(activity: BaseActivity, url: String?, imageView: PorterShapeImageView){
            if (!url.isNullOrBlank()){
                Glide.with(activity)
                    .load(url)
                    // .placeholder(R.drawable.profile)
                    // .error(R.drawable.profile)
                    .into(imageView)
            }
        }

    }
}