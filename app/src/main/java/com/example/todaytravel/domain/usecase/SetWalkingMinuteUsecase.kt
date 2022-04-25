package com.example.todaytravel.domain.usecase

import com.example.todaytravel.data.repo.SharedPreferenceRepository
import javax.inject.Inject

class SetWalkingMinuteUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun setWalkingMinute(m: Int) {
        repo.setWalkingMinute(m)
    }
}