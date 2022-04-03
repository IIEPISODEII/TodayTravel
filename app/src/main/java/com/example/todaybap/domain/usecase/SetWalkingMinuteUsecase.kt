package com.example.todaybap.domain.usecase

import com.example.todaybap.domain.repo.SharedPreferenceRepository
import javax.inject.Inject

class SetWalkingMinuteUsecase @Inject constructor(private val repo: SharedPreferenceRepository) {
    fun setWalkingMinute(m: Int) {
        repo.setWalkingMinute(m)
    }
}