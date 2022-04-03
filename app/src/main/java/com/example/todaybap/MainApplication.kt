package com.example.todaybap

import android.app.Application
import com.example.todaybap.domain.repo.SharedPreferenceRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    // 어플 시작 시 실행됨
    override fun onCreate() {
        super.onCreate()
    }
}