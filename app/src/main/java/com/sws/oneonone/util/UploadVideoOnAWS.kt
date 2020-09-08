package com.sws.oneonone.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowManager
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.sws.oneonone.fragment.ChatMessageFragment
import com.sws.oneonone.fragment.NewChallengeFragment
import com.sws.oneonone.fragment.VideoPreviewFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class UploadVideoOnAWS {
    companion object {
        var url: String? = null
        var activity: BaseActivity? = null
        var thumbNail: String? = null
        var videoFile:File? = null
        fun uploadVideo(activity: BaseActivity, str: String, fragment: ChatMessageFragment) {
            this.url = str
            this.activity = activity
            Loader?.showLoader(activity!!)

            // Create thumbnail
            try {
                var thumb: Bitmap?  = ThumbnailUtils.createVideoThumbnail(str, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                val uri = getImageUri(activity, thumb!!)
                thumbNail = getRealPathFromURI(activity, uri)
                //imgFarmerVideo.setImageBitmap(thumb)
            } catch (e: Exception) {
                e.printStackTrace()
            }


            val credentialsProvider = CognitoCachingCredentialsProvider(
                ApplicationLoader.applicationContext!!,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "images" + file.getName(), file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.getKey()
                        //set Image in imageView

                        val nail = observer
                        // Loader?.hideLoader(activity!!)
                        uploadThumbNailImage(activity!!, thumbNail!!, url, fragment)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader?.hideLoader(activity!!)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader?.hideLoader(activity!!)
                    Log.e("responseError", "Error  : " + ex.message)
                    ex.printStackTrace()
                }
            })
        }


        fun uploadVideo(activity: BaseActivity, str: String, fragment: NewChallengeFragment, videoFile : File) {
            this.url = str
            this.activity = activity
            var picturePath: File? = null
            Loader.showLoader(activity)

            // Create thumbnail
            try {
                val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
                val width = metrics.widthPixels
                val height = metrics.heightPixels
                val size = Size(width,height)
                val thumb: Bitmap?
                if (Build.VERSION.SDK_INT >= 29) {
                    thumb  = ThumbnailUtils.createVideoThumbnail(videoFile,size,null)
                }else{
                    thumb   = ThumbnailUtils.createVideoThumbnail(videoFile.absolutePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                }
                val name = UUID.randomUUID().toString().toUpperCase()
                 picturePath = AndroidUtilities().getSavedBitmapPath(name, activity, thumb!!)
                //imgFarmerVideo.setImageBitmap(thumb)
            } catch (e: Exception) {
                e.printStackTrace()
            }


            val credentialsProvider = CognitoCachingCredentialsProvider(
                ApplicationLoader.applicationContext!!,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "videos/" + file.name, file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.getKey()
                        // Loader?.hideLoader(activity!!)
                        uploadThumbNailImage(activity, picturePath!!.absolutePath , url, fragment)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader.hideLoader(activity)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader?.hideLoader(activity!!)
                    Log.e("responseError", "Error  : " + ex.message)
                    ex.printStackTrace()
                }
            })
        }
        fun uploadVideo(activity: BaseActivity, str: String, fragment: VideoPreviewFragment, videoFile : File) {
            this.url = str
            this.activity = activity
            var picturePath: File? = null
            Loader.showLoader(activity)

            // Create thumbnail
            try {
                val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
                val width = metrics.widthPixels
                val height = metrics.heightPixels
                val size = Size(width,height)
                val thumb: Bitmap?
                if (Build.VERSION.SDK_INT >= 29) {
                    thumb  = ThumbnailUtils.createVideoThumbnail(videoFile,size,null)
                }else{
                    thumb   = ThumbnailUtils.createVideoThumbnail(videoFile.absolutePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND)
                }
                val name = UUID.randomUUID().toString().toUpperCase()
                picturePath = AndroidUtilities().getSavedBitmapPath(name, activity, thumb!!)

                // thumbNail = getRealPathFromURI(activity, uri)
                //imgFarmerVideo.setImageBitmap(thumb)
            } catch (e: Exception) {
                e.printStackTrace()
            }


            val credentialsProvider = CognitoCachingCredentialsProvider(
                ApplicationLoader.applicationContext!!,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "videos/" + file.name, file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.getKey()
                        // Loader?.hideLoader(activity!!)
                        uploadThumbNailImage(activity, picturePath!!.absolutePath , url, fragment)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader.hideLoader(activity)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader?.hideLoader(activity!!)
                    Log.e("responseError", "Error  : " + ex.message)
                    ex.printStackTrace()
                }
            })
        }



        fun uploadThumbNailImage(activity: BaseActivity, str: String, videoUrl: String?, fragment: ChatMessageFragment) {
            this.url = str
            val credentialsProvider = CognitoCachingCredentialsProvider(
                ApplicationLoader.applicationContext,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "images" + file.getName(), file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.getKey()
                        //set Image in imageView

                        Loader?.hideLoader(activity!!)
                        fragment?.onStateVideoComplete(videoUrl, url)
                        Log.e("responseProgress", "Image Url : " + url)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader?.hideLoader(activity!!)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader?.hideLoader(activity!!)
                    Log.e("responseError", "Error  : " + ex.message)
                    ex.printStackTrace()
                }
            })
        }


        fun getImageUri(inContext:BaseActivity, inImage:Bitmap):Uri {
            val bytes = ByteArrayOutputStream()
           // inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val tsLong = System.currentTimeMillis() / 1000
            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, tsLong.toString(), null)
            return Uri.parse(path)
        }

        fun getRealPathFromURI(activity: BaseActivity, uri:Uri): String? {
           // val cursor = getContentResolver().query(uri, null, null, null, null)
            val cursor = activity.contentResolver.query(uri, null, null, null, null)
            cursor?.moveToFirst()
            val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val string: String?  = cursor?.getString(idx!!) //cursor?.getString(idx)
            return string
        }





        fun uploadThumbNailImage(activity: BaseActivity, str: String, videoUrl: String?, fragment: NewChallengeFragment) {
            this.url = str
              val credentialsProvider = CognitoCachingCredentialsProvider(
                  ApplicationLoader.applicationContext!!,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "thumbnails/" + file.name, file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.key
                        //set Image in imageView

                        Loader.hideLoader(activity)
                        fragment.onStateVideoComplete(videoUrl, url)
                        Log.e("responseProgress", "Image Url : " + url)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader.hideLoader(activity)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader.hideLoader(activity)
                    Log.e("responseError", "Error  : " + ex.message)
                    Utils.showToast(activity,"thumbnailUploadError",false)
                    ex.printStackTrace()
                }
            })
        }

        fun uploadThumbNailImage(activity: BaseActivity, str: String, videoUrl: String?, fragment: VideoPreviewFragment) {
            this.url = str
            val credentialsProvider = CognitoCachingCredentialsProvider(
                ApplicationLoader.applicationContext!!,
                AppStaticData.POOL_ID, Regions.US_EAST_1
            )
            val s3 = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility(s3, activity)
            val file = File(str)
            val observer = transferUtility.upload(
                AppStaticData.BUSKET_NAME,
                "thumbnails/" + file.name, file, CannedAccessControlList.PublicRead
            )
            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state === TransferState.COMPLETED) {
                        val url = AppStaticData.AWS_URL + observer.key
                        //set Image in imageView

                        Loader.hideLoader(activity)
                        fragment.onStateVideoComplete(videoUrl, url)
                        Log.e("responseProgress", "Image Url : " + url)

                        //we just need to share this File url with Api service request.
                    } else if (state === TransferState.FAILED) {
                        Log.e("responseProgress", "FAILED")
                        Loader.hideLoader(activity)
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val done = (((bytesCurrent.toDouble() / bytesTotal) * 100.0).toInt())
                    Log.d("responseProgress", "UPLOAD - - ID: $id, percent done = $done")
                }

                override fun onError(id: Int, ex: Exception) {
                    Loader.hideLoader(activity)
                    Log.e("responseError", "Error  : " + ex.message)
                    Utils.showToast(activity,"thumbnailUploadError",false)
                    ex.printStackTrace()
                }
            })
        }

    }
}