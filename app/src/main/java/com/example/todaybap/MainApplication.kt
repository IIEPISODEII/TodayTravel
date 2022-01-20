package com.example.todaybap

import android.app.Application
import com.example.todaybap.repo.SharedPreferenceRepository

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // SharedPreferenceRepository 초기화
        SharedPreferenceRepository.initialize(applicationContext)
    }
}