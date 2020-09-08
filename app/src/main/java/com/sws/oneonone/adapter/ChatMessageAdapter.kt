package com.sws.oneonone.adapter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseError
import com.google.firebase.database.*
import com.sws.oneonone.R
import com.sws.oneonone.databinding.LayoutChatMessageBinding
import com.sws.oneonone.dialog.VideoViewDialog
import com.sws.oneonone.firebaseModel.ChatUserModel
import com.sws.oneonone.firebaseModel.UserModel
import com.sws.oneonone.util.AppStaticData
import com.sws.oneonone.util.BaseActivity
import com.sws.oneonone.util.ShowingImage
import com.sws.oneonone.util.Utils
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatMessageAdapter(activity: BaseActivity, var modelList: ArrayList<ChatUserModel>?, val imageUrl: String?):
    RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>(){

    private var activity: BaseActivity? = activity
    private var mFirebaseDatabase: DatabaseReference? = null
    private var mFirebaseInstance: FirebaseDatabase? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val layoutChatMessageBinding = DataBindingUtil.inflate<LayoutChatMessageBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.layout_chat_message, viewGroup, false
        )

        return ViewHolder(layoutChatMessageBinding)
    }

    override fun onBindViewHolder(ViewHolder: ViewHolder, i: Int) {
        ViewHolder.layoutChatMessageBinding!!.chatImage?.visibility = View.GONE
        ViewHolder.layoutChatMessageBinding!!.videoThumbNail?.visibility = View.GONE
        ViewHolder.layoutChatMessageBinding!!.chatImage2?.visibility = View.GONE
        ViewHolder.layoutChatMessageBinding!!.myVideoThumbNail?.visibility = View.GONE

        val model : ChatUserModel? = modelList?.get(i)?.getChatUserModel()
        if(model?.getMessage().isNullOrEmpty() && model?.getImage().isNullOrEmpty() && model?.getVideo().isNullOrEmpty()) {
            ViewHolder.layoutChatMessageBinding!!.topLayout?.visibility = View.GONE
        } else {
            ViewHolder.layoutChatMessageBinding!!.topLayout?.visibility = View.VISIBLE
            if (modelList?.get(i)?.getUserId().equals(model?.getSenderId())) {
                ViewHolder.layoutChatMessageBinding!!.layoutYourMsg?.visibility = View.GONE
                ViewHolder.layoutChatMessageBinding!!.layoutMyMsg?.visibility = View.VISIBLE
                if (!model?.getImage().isNullOrEmpty()) {
                    ViewHolder.layoutChatMessageBinding!!.chatImage2?.visibility = View.VISIBLE
                    ShowingImage?.showBannerImage(
                        activity!!,
                        model?.getImage(),
                        ViewHolder.layoutChatMessageBinding!!.chatImage2
                    )
                } else if (!model?.getVideoThumb().isNullOrEmpty()) {
                    ViewHolder.layoutChatMessageBinding!!.layoutVideoMe?.visibility = View.VISIBLE
                    ViewHolder.layoutChatMessageBinding!!.myVideoThumbNail?.visibility = View.VISIBLE
                    ShowingImage?.showChatImage(
                        activity!!,
                        model?.getVideoThumb(),
                        ViewHolder.layoutChatMessageBinding!!.myVideoThumbNail
                    )
                } else {
                  //  ViewHolder.layoutChatMessageBinding!!.chatImage2?.visibility = View.GONE
                  //  ViewHolder.layoutChatMessageBinding!!.myVideoThumbNail?.visibility = View.GONE
                }
                val lastSeen: Long? = Utils().getMilliFromDate(model?.getTime())
                val msgTime: String? = Utils().convertChatData(lastSeen)
                messageStatus(
                    model?.getMsgStatus(),
                    ViewHolder.layoutChatMessageBinding!!.myMsgDate, msgTime
                )
            } else {
                ViewHolder.layoutChatMessageBinding!!.layoutYourMsg?.visibility = View.VISIBLE
                ViewHolder.layoutChatMessageBinding!!.layoutMyMsg?.visibility = View.GONE
                var readableStatus: String? = "read"
                if (!model?.getMessage().isNullOrEmpty()) {
                    readableStatus = "read"
                } else if (!model?.getImage().isNullOrEmpty()) {
                    ViewHolder.layoutChatMessageBinding!!.chatImage?.visibility = View.VISIBLE
                    ShowingImage?.showBannerImage(
                        activity!!,
                        model?.getImage(),
                        ViewHolder.layoutChatMessageBinding!!.chatImage
                    )
                    readableStatus = "read" //"opened"

                } else if (!model?.getVideoThumb().isNullOrEmpty()) {
                    ViewHolder.layoutChatMessageBinding!!.layoutVideo?.visibility = View.VISIBLE
                    ViewHolder.layoutChatMessageBinding!!.videoThumbNail?.visibility = View.VISIBLE

                    ShowingImage?.showChatImage(
                        activity!!,
                        model?.getVideoThumb(),
                        ViewHolder.layoutChatMessageBinding!!.videoThumbNail
                    )
                    readableStatus = "read" //"opened"
                } else {
                  //  ViewHolder.layoutChatMessageBinding!!.chatImage?.visibility = View.GONE
                   // ViewHolder.layoutChatMessageBinding!!.videoThumbNail?.visibility = View.GONE
                }

                if(model?.getIsGroup()!!){
                    updateGroupChatMessage(
                        activity!!,
                        modelList?.get(i)?.getChatKey(),
                        modelList?.get(i)?.getMsgKey(),
                        readableStatus
                    )
                } else {
                    updateUserChatMessage(
                        activity!!,
                        modelList?.get(i)?.getChatKey(),
                        modelList?.get(i)?.getMsgKey(),
                        readableStatus
                    )
                }

                if(model?.getIsGroup()!!){
                    showGroupMemberDetails(model?.getSenderId(), ViewHolder.layoutChatMessageBinding!!.profileImg, ViewHolder.layoutChatMessageBinding!!.tvUserName)
                } else {
                    if (!imageUrl.isNullOrEmpty()) {
                        ShowingImage?.showImage(
                            activity!!,
                            imageUrl,
                            ViewHolder.layoutChatMessageBinding!!.profileImg
                        )
                    }
                }
            }
        }


        //ViewHolder.layoutChatMessageBinding!!.chatImage
        ViewHolder.layoutChatMessageBinding!!.chatImage?.setOnClickListener{
            val modelViews : ChatUserModel? = modelList?.get(i)?.getChatUserModel()
            val  videoViewDialog = VideoViewDialog.newInstance(activity!!, "image", modelViews?.getImage(), "")
            videoViewDialog.show(activity!!.supportFragmentManager, "videiViewDialog")

        }
        ViewHolder.layoutChatMessageBinding!!.chatImage2?.setOnClickListener{
            val modelViews : ChatUserModel? = modelList?.get(i)?.getChatUserModel()
            val  videoViewDialog = VideoViewDialog.newInstance(activity!!, "image", modelViews?.getImage(),"")
            videoViewDialog.show(activity!!.supportFragmentManager, "videiViewDialog")
        }

        ViewHolder.layoutChatMessageBinding!!.videoThumbNail?.setOnClickListener {
            val modelViews : ChatUserModel? = modelList?.get(i)?.getChatUserModel()
            val  videoViewDialog = VideoViewDialog.newInstance(activity!!, "video", modelViews?.getVideo(), modelViews?.getVideoThumb())
            videoViewDialog.show(activity!!.supportFragmentManager, "videiViewDialog")
        }
        ViewHolder.layoutChatMessageBinding!!.myVideoThumbNail?.setOnClickListener {
            val modelViews : ChatUserModel? = modelList?.get(i)?.getChatUserModel()
            val  videoViewDialog = VideoViewDialog.newInstance(activity!!, "video", modelViews?.getVideo(), modelViews?.getVideoThumb())
            videoViewDialog.show(activity!!.supportFragmentManager, "videiViewDialog")
        }
        ViewHolder.layoutChatMessageBinding!!.chatUserModel = model
    }


    override fun getItemCount(): Int {
        return modelList!!.size
    }


    inner class ViewHolder(val layoutChatMessageBinding: LayoutChatMessageBinding) :
        RecyclerView.ViewHolder(layoutChatMessageBinding.root)


    fun updateUserChatMessage(activity: BaseActivity?, chatWith: String?, chatKey: String?, status: String?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }
        // updating the user via child nodes
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        mFirebaseDatabase?.child(chatWith!!)?.child(chatKey!!)?.child("read")?.setValue("1")
        mFirebaseDatabase?.child(chatWith!!)?.child(chatKey!!)?.child("msgStatus")?.setValue(status)

    }

    fun updateGroupChatMessage(activity: BaseActivity?, chatWith: String?, chatKey: String?, status: String?) {
        mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        if (Utils.isNetworkAvailable(activity!!)) {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(false)
        } else {
            val scoresRef = FirebaseDatabase.getInstance().getReference("Chat")
            scoresRef.keepSynced(true)
        }
        // updating the user via child nodes
        mFirebaseDatabase = mFirebaseInstance?.getReference("Chat")
        mFirebaseDatabase?.child(chatWith!!)?.child(chatKey!!)?.child("member")?.child(AppStaticData().getModel()?.result?.id!!)?.child("read")?.setValue("1")
        mFirebaseDatabase?.child(chatWith!!)?.child(chatKey!!)?.child("member")?.child(AppStaticData().getModel()?.result?.id!!)?.child("msgStatus")?.setValue("read")
       // mFirebaseDatabase?.child(chatWith!!)?.child(chatKey!!)?.child("msgStatus")?.setValue(status)

    }

    fun messageStatus(status: String?, textView: TextView, msgTime: String?) {
        when (status) {
            "sent" -> { // tap to chat
                textView?.text = "Send"+" - "+msgTime// "Tap to chat"
                textView?.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.opend_chat,
                    //  R.drawable.tap_chat,
                    0,
                    0,
                    0
                )
            }
            "delivered" -> {
                textView?.text = "Delivered"+" - "+msgTime
                textView?.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.deliver_icon,
                    0,
                    0,
                    0
                )
            }
            "read" -> { //
                textView?.text = "Read"+" - "+msgTime
                textView?.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.opend_chat,
                    0,
                    0,
                    0
                )
            }
            "faild" -> { //
                textView?.text = "Faild - Tap to retry"+" - "+msgTime
                textView?.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.failed_icon,
                    0,
                    0,
                    0
                )
            }
        }
    }

    fun showGroupMemberDetails(userId: String?, imageView: CircleImageView, textView: TextView){
        mFirebaseInstance = FirebaseDatabase.getInstance()
        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance?.getReference("Users")
        var mDatabase = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("Users").child(userId!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    if(user != null) {
                        textView?.text = user?.getUserName()
                        if (!user?.getUserImg().isNullOrEmpty()) {
                            ShowingImage?.showImage(
                                activity!!,
                                user?.getUserImg(),
                                imageView
                            )
                        }

                    }
                }
            })
    }
}