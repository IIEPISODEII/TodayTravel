package com.example.todaybap.repo

import com.example.todaybap.retrofit.RetrofitClient
import com.example.todaybap.retrofit.model.NaverGCDTO
import com.naver.maps.geometry.LatLng
import retrofit2.Response
import util.Constants
import java.lang.IllegalStateException

class NaverMapRepository {

    companion object {
        private var sInstance: NaverMapRepository? = null

        fun getInstance(): NaverMapRepository {
            if (sInstance == null) sInstance = NaverMapRepository()
            return sInstance ?: throw IllegalStateException("NaverMapRepository must be initialized")
        }
    }
    private val retrofitClient = RetrofitClient.getRetrofitInstance()

    suspend fun getCoordinateInfo(query: LatLng): Response<NaverGCDTO> {
        return retrofitClient?.getInfo(
            clientID = Constants.CLIENT_ID,
            clientKey = Constants.CLIENT_SECRET,
            coordinates = query.longitude.toString() + "," + query.latitude.toString(),
            orders = "legalcode,addr,admcode,roadaddr",
            form = "json"
        )!!
    }
}