package com.sws.oneonone.viewModel

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Telephony
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sws.oneonone.dialog.AlertDialog
import com.sws.oneonone.dialog.PreviewBottomDialog
import com.sws.oneonone.dialog.ProfileBottomDialog
import com.sws.oneonone.fragment.EditProfileFragment
import com.sws.oneonone.fragment.ProfileFragment
import com.sws.oneonone.util.AndroidUtilities
import com.sws.oneonone.util.BaseActivity
import java.io.File


class CameraUIVM: ViewModel() {
    private  var commanMLD: MutableLiveData<String>? = null
    var activity: BaseActivity? = null
    val msg = "1on1 - Challenge your friends"
    val mInvitationUrl = "http://onelink.to/aadz6r"
    var shareMessage = "\n" + msg + "\n" + mInvitationUrl + "\n"

    val comman: MutableLiveData<String>
        get() {
            if (commanMLD == null)
            {
                commanMLD = MutableLiveData()
            }
            return commanMLD as MutableLiveData<String>
        }

    fun onBackClick() {
        comman.value = "back"
    }
    // call snapchat app
    fun onSnapChatClick() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setPackage("com.snapchat.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            intent.type = "text/plain"
            activity!!. startActivity(intent)
        } catch (e: Exception) {
            openAlertDialog("Can't be shared.Snapchat isn't Installed")
        }

    }
    // call whatsapp app
    fun onWhatsappClick() {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.setPackage("com.whatsapp")
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                intent.type = "text/plain"
                activity!!.startActivity(intent)
            } catch (e: Exception) {
                openAlertDialog("Can't be shared.Watsapp isn't Installed")
            }
        }


    fun onTiktokClick(){
        try {
            val uri: Uri = Uri.parse("https://vm.tiktok.com/tiktok")//+ "tiktokExtension")
            val tiktokIntent =
                Intent(Intent.ACTION_VIEW, uri)
            tiktokIntent.setPackage("com.zhiliaoapp.musically")
            activity?.startActivity(tiktokIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "TikTok application isn't installed!", Toast.LENGTH_SHORT).show()
            //if the app doesn't exist do some stuff , like redirection to playstore.....
        }
    }
    fun onProfileClick() {
        val  videoBottomSheetDialog = ProfileBottomDialog.newInstance(activity!!,"",null,null)
        videoBottomSheetDialog.show(activity!!.supportFragmentManager, ProfileBottomDialog().TAG)
    }

    fun onEditProfileClick() {
        activity?.replaceFragment(EditProfileFragment())
    }

    fun onShareClick(){
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        activity!!.startActivity(Intent.createChooser(intent, "choose one"))
    }

    fun onSmsClick(){
        try {
            val defaultSmsPackageName: String = Telephony.Sms.getDefaultSmsPackage(activity) // Need to change the build to API 19
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            if (defaultSmsPackageName.isNotEmpty())  {
                sendIntent.setPackage(defaultSmsPackageName)
            }
            activity!!.startActivity(sendIntent)
        } catch (e: Exception) {
        }
    }


    fun onSnapchatVideoClick(){
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://snapchat.com/add")
            )
            intent.setPackage("com.snapchat.android")
            activity!!.startActivity(intent)
        } catch (e: Exception) {
            AndroidUtilities().openAlertDialog("Can't be shared.Snapchat isn't Installed",activity!!)
        }
    }


    fun openAlertDialog(txtmsg :String){
        val myDialog = AlertDialog.newInstance(activity!!,txtmsg)
        myDialog.show(activity!!.supportFragmentManager, "Hello Testing")
    }


}