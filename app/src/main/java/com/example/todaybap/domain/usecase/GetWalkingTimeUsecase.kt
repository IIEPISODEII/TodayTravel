package com.example.todaybap.domain.usecase

import android.content.SharedPreferences
import com.example.todaybap.domain.repo.SharedPreferenceRepository
import javax.inject.Inject

class GetWalkingTimeUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun getWalkingTime(): Int {
        return repo.getWalkingTime()
    }
}