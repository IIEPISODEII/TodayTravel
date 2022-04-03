package com.example.todaybap.domain.repo

import android.os.Build
import com.example.todaybap.BuildConfig
import com.example.todaybap.data.naverapi.NaverGCDTO
import com.example.todaybap.data.naverapi.retrofit.NaverMapService
import com.naver.maps.geometry.LatLng
import retrofit2.Response
import javax.inject.Inject

class NaverMapRepository @Inject constructor (
    private val service: NaverMapService
) {

    suspend fun getCoordinateInfo(query: LatLng): Response<NaverGCDTO> {
        return service.getInfo(
            clientID = BuildConfig.CLIENT_ID,
            clientKey = BuildConfig.CLIENT_KEY,
            coordinates = query.longitude.toString() + "," + query.latitude.toString(),
            orders = "legalcode,addr,admcode,roadaddr",
            form = "json"
        )
    }
}