package com.example.todaybap.retrofit

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import util.Constants

object RetrofitClient {
    var retrofit: Retrofit? = null

    fun getRetrofitInstance(): IRetrofit? {
        val okHTTPClient = OkHttpClient.Builder()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(
                "LOGGING INTERCEPTOR",
                "RetrofitClient - $message"
            )
        }

        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        okHTTPClient.addInterceptor(loggingInterceptor)

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.NAVER_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHTTPClient.build())
                .build()
        }
        return retrofit?.create(IRetrofit::class.java)
    }
}