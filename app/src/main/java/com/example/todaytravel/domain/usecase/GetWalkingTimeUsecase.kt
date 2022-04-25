package com.example.todaytravel.domain.usecase

import com.example.todaytravel.data.repo.SharedPreferenceRepository
import javax.inject.Inject

class GetWalkingTimeUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun getWalkingTime(): Int {
        return repo.getWalkingTime()
    }
}