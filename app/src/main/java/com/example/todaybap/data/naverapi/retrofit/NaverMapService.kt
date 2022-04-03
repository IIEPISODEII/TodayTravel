package com.example.todaybap.data.naverapi.retrofit

import android.util.Log
import com.example.todaybap.data.naverapi.NaverGCDTO
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverMapService {
    @GET("map-reversegeocode/v2/gc")
    suspend fun getInfo(
        @Header("X-NCP-APIGW-API-KEY-ID") clientID: String,
        @Header("X-NCP-APIGW-API-KEY") clientKey: String,
        @Query("coords") coordinates: String,
        @Query("orders") orders: String,
        @Query("output") form: String
    ): Response<NaverGCDTO>

    companion object {
        private val NAVER_MAP_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

        fun getRetrofitInstance(): NaverMapService {
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

            return Retrofit.Builder()
                .baseUrl(NAVER_MAP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHTTPClient.build())
                .build()
                .create(NaverMapService::class.java)
        }
    }
}