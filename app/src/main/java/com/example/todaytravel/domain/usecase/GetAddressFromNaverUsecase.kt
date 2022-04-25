package com.example.todaytravel.domain.usecase

import com.example.todaytravel.data.navermapapi.retrofit.NaverGCDTO
import com.example.todaytravel.data.repo.NaverMapRepository
import com.naver.maps.geometry.LatLng
import retrofit2.Response
import javax.inject.Inject

class GetAddressFromNaverUsecase @Inject constructor(private val repo: NaverMapRepository) {
    suspend fun getAddressFromNaver(query: LatLng): Response<NaverGCDTO> {
        return repo.getCoordinateInfo(query = query)
    }
}