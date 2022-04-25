package com.example.todaytravel

import android.app.Application
import android.content.pm.PackageManager
import android.util.Base64.encode
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.HiltAndroidApp
import java.security.MessageDigest
import java.util.*


@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.KAKAO_NATIVE_APP_KEY
        )
    }
}