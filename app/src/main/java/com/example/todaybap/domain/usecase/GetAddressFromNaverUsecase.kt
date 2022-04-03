package com.example.todaybap.domain.usecase

import com.example.todaybap.data.naverapi.NaverGCDTO
import com.example.todaybap.domain.repo.NaverMapRepository
import com.naver.maps.geometry.LatLng
import retrofit2.Response
import javax.inject.Inject

class GetAddressFromNaverUsecase @Inject constructor(private val repo: NaverMapRepository) {
    suspend fun getAddressFromNaver(query: LatLng): Response<NaverGCDTO> {
        return repo.getCoordinateInfo(query = query)
    }
}