package com.sws.oneonone.retrofit


import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


class ApiClient {

    var BASE_URL = "http://54.169.45.113:3006/"
    var retrofit: Retrofit? = null
    var okHttpClient: OkHttpClient = OkHttpClient()



    var gson = GsonBuilder()
            .setLenient()
            .create()

    fun getClient(isForVideo:Boolean,base_url:String): Retrofit {
        if(isForVideo){
          BASE_URL = "https://api.cloudflare.com/client/v4/accounts/0f3a9f41d8b87e18512a14911083727d/"
        }else if(base_url.isNotEmpty()){
            BASE_URL = base_url + "/"
        }else{
            BASE_URL = "http://54.169.45.113:3006/"
        }
        okHttpClient =  OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100,TimeUnit.SECONDS).build()
        //.build()

            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(nullOnEmptyConverterFactory)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

        return retrofit!!
    }

    val nullOnEmptyConverterFactory = object : Converter.Factory() {
        fun converterFactory() = this
        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object : Converter<ResponseBody, Any?> {
            val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
            override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
        }
    }
}
