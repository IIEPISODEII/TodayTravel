package com.sb.todaytravel

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.naver.maps.map.NaverMapSdk
import com.sb.todaytravel.feature.travel_manage.FusedLocationProviderManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_MAP_CLIENT_ID)
        FusedLocationProviderManager.init(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        FusedLocationProviderManager.unregisterLocationCallback()
        FusedLocationProviderManager.terminate()
    }

    @Inject lateinit var workerFactory: HiltWorkerFactory
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}