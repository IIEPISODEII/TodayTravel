package com.example.todaytravel.data.repo

import com.example.todaytravel.BuildConfig
import com.example.todaytravel.data.navermapapi.retrofit.NaverGCDTO
import com.example.todaytravel.data.navermapapi.retrofit.NaverMapService
import com.naver.maps.geometry.LatLng
import retrofit2.Response
import javax.inject.Inject

class NaverMapRepository @Inject constructor (
    private val service: NaverMapService
) {

    suspend fun getCoordinateInfo(query: LatLng): Response<NaverGCDTO> {
        return service.getInfo(
            clientID = BuildConfig.NAVER_MAP_CLIENT_ID,
            clientKey = BuildConfig.NAVER_MAP_CLIENT_KEY,
            coordinates = query.longitude.toString() + "," + query.latitude.toString(),
            orders = "legalcode,addr,admcode,roadaddr",
            form = "json"
        )
    }
}