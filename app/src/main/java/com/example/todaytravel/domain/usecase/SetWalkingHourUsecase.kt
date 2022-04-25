package com.example.todaytravel.domain.usecase

import com.example.todaytravel.data.repo.SharedPreferenceRepository
import javax.inject.Inject

class SetWalkingHourUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun setTravelHour(h: Int) {
        repo.setWalkingHour(h)
    }
}