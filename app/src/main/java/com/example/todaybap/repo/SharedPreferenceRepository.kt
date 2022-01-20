package com.example.todaybap.repo

import android.content.Context
import java.lang.IllegalStateException

class SharedPreferenceRepository(context: Context) {
    private val SHARED_PREFERENCE = "mSharedPreference"
    private val TIME_WALKING_HOUR = "walking-hour"
    private val TIME_WALKING_MINUTE = "walking-minute"

    companion object {
        private var sInstance: SharedPreferenceRepository? = null

        // Repository 인스턴스를 싱글턴으로 생성
        fun initialize(mContext: Context) {
            if (sInstance == null) sInstance = SharedPreferenceRepository(mContext)
        }
        // Repository 인스턴스 반환
        fun getInstance(): SharedPreferenceRepository {
            return sInstance ?: throw IllegalStateException("SharedPreferenceRepository is not initialized")
        }
    }

    private val sharedPreference = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

    fun setWalkingHour(time: String) {
        val mTime: Int = time.toInt()
        sharedPreference.edit().putInt(TIME_WALKING_HOUR, mTime).apply()
    }

    fun setWalkingMinute(time: String) {
        val mTime: Int = time.toInt()
        sharedPreference.edit().putInt(TIME_WALKING_MINUTE, mTime).apply()
    }

    // 분 단위로 표현
    fun getWalkingTime(): Int {
        return sharedPreference.getInt(TIME_WALKING_MINUTE, 0) + sharedPreference.getInt(
            TIME_WALKING_HOUR,
            0
        ) * 60 + 10
    }
}