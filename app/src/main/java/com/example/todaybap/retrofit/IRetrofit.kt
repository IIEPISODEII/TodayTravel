package com.example.todaybap.retrofit

import com.example.todaybap.retrofit.model.NaverGCDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface IRetrofit {
    @GET("map-reversegeocode/v2/gc")
    suspend fun getInfo(
        @Header("X-NCP-APIGW-API-KEY-ID") clientID: String,
        @Header("X-NCP-APIGW-API-KEY") clientKey: String,
        @Query("coords") coordinates: String,
        @Query("orders") orders: String,
        @Query("output") form: String
    ): Response<NaverGCDTO>
}