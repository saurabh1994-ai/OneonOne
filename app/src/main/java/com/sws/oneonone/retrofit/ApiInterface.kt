package com.sws.oneonone.retrofit


import com.google.gson.JsonObject
import com.sws.oneonone.firebase_notification.RootModel
import com.sws.oneonone.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    // User SignUp
    @POST("user/userSignUp")
    fun userSignUp(@Body body: JsonObject): Call<SignUpModel>

    @POST("user/userLogin")
    fun userLogin(@Body body: JsonObject): Call<SignUpModel>

    // User Profile Update API
    @POST("user/updateProfile")
    fun updateProfile(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<SignUpModel>

    // User Logout API
    @POST("user/userLogout")
    fun userLogout(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<SignUpModel>

    // User Change Password API
    @POST("user/changePassword")
    fun changePassword(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<SignUpModel>

    // User get all challengers API
    @POST("challenges/getAllChallenges")
    fun getAllChallenges(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<ExploreModel>

    // User Your challengers challenge API (Explore Your Challengers)
    @POST("challenges/getCreatedChallenges")
    fun getCreatedChallenges(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<CreatedChallengeDataModel>


    // User Your challengers challenge API (Explore Your Challengers)
    @POST("notifications/getAllNotifications")
    fun getAllNotifications(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<NotificationResponseModel>

    // User Your challengers challenge API (Explore Your Challengers)
    @POST("follow/followSuggestions")
    fun getAllUserList(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<MyFollowingModel>




 // User Your challengers challenge API (Explore Your Challengers)
    @POST("user/forgotPassword")
    fun forgotPassword  (@Body body: JsonObject): Call<SignUpModel>

    @POST("follow/followers")
    fun setFollowUnFollow(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SignUpModel>

    @POST("challenges/challenge")
    fun createChallenge(@Header("Authorization") str : String?, @Body body: JsonObject): Call<CareateChallengeModel>

    @POST("challenges/getChallenges")
    fun getChallenges(@Header("Authorization") str : String?, @Body body: JsonObject): Call<ViewExploreModel>


    @POST("challenges/getUserProfileChallenge")
    fun getUserProfileChallenge(@Header("Authorization") str : String?, @Body body: JsonObject): Call<ProfileChallengeModel>


    @POST("challenges/getcomment")
    fun getComments(@Header("Authorization") str : String?, @Body body: JsonObject): Call<CommentsModel>

    @POST("votes/vote")
    fun addvoteChallenge(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("user/userMessageList")
    fun userMessageList(@Header("Authorization") str : String?, @Body body: JsonObject): Call<AllMessageUserModel>

    @POST("challenges/likeUnlike")
    fun likeUnlike(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("challenges/comments")
    fun addComments(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("challenges/getlikes")
    fun getLikes(@Header("Authorization") str : String?, @Body body: JsonObject): Call<LikesModel>

    @POST("challenges/startVoting")
    fun startVoting(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("votes/getVotes")
    fun getVotes(@Header("Authorization") str : String?, @Body body: JsonObject): Call<LikesModel>

    @POST("views/getViews")
    fun getViews(@Header("Authorization") str : String?, @Body body: JsonObject): Call<LikesModel>

    @POST("views/viewCount")
    fun addViews(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("challenges/searchhashtag")
    fun getHashtag(@Header("Authorization") str : String?, @Body body: JsonObject): Call<HastagModel>

    @POST("user/check_existsUser")
    fun checkIfUserExist(@Body body: JsonObject): Call<SuccessModel>

    @POST("challenges/checkChallenge")
    fun checkChallenge(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("report/reportUser")
    fun reportUser(@Header("Authorization") str : String?, @Body body: JsonObject): Call<SuccessModel>

    @POST("user/socialLogin")
    fun socialLogin(@Body body: JsonObject): Call<SignUpModel>

    @POST("user/updateAccountPrivacy")
    fun updateAccountPrivacy(@Header("Authorization") str : String?,@Body body: JsonObject): Call<SignUpModel>

    @POST("follow/getChallengersList")
    fun getChallengersList(@Header("Authorization") str : String?,@Body body: JsonObject): Call<MyFollowingModel>

    @POST("user/pushNotification")
    fun onOffPushNoti(@Header("Authorization") str : String?,@Body body: JsonObject): Call<SignUpModel>

    @POST("follow/myFollowings")
    fun myFollowings(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<MyFollowingModel>

    @POST("follow/myFollowers")
    fun myFollowers(@Header("Authorization") str : String?,  @Body body: JsonObject): Call<MyFollowingModel>



    @Headers("Authorization:key=AAAA0gUXtBY:APA91bF8zcroIxjlxEd7pnZecHsTxzb-WXgv3AKUTVsuUScdLMeMjy3JjCXw_RK_CHvCum4ekFc3VfGxdbU4BTmYDp782roIVb29JZTKEDMAIeY-m05GWTIsKkQYNVXCm0QwPmTifW6A",
        "Content-Type:application/json")
    @POST("fcm/send")
    fun chatNotification(@Body body: RootModel): Call<ResponseBody>


    @POST("stream/direct_upload")
    fun getLinkForVideo(@Header("X-Auth-Key") key : String?,@Header("X-Auth-Email") email : String?,@Body body: JsonObject): Call<VideoLInkModel>

    @Multipart
    @POST(".")
    fun uploadVideo(@Part file: MultipartBody.Part): Call<ResponseBody>

    @GET(".")
    fun  getVideoUrl(@Header("Authorization") token :String?, @Header("X-Auth-Key") key : String?, @Header("X-Auth-Email") email : String?):Call<CFVideoUrlModel>





}