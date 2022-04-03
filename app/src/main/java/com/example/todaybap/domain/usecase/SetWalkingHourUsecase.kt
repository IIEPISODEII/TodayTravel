package com.example.todaybap.domain.usecase

import com.example.todaybap.domain.repo.SharedPreferenceRepository
import javax.inject.Inject

class SetWalkingHourUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun setTravelHour(h: Int) {
        repo.setWalkingHour(h)
    }
}