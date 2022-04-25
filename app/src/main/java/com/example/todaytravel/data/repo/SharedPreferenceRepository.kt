package com.example.todaytravel.data.repo

import android.content.Context

class SharedPreferenceRepository(context: Context) {
    private val TIME_WALKING_HOUR = "walking-hour"
    private val TIME_WALKING_MINUTE = "walking-minute"

    companion object {
        const val SHARED_PREFERENCE = "mSharedPreference"

        // Repository 인스턴스 반환
        fun getInstance(context: Context): SharedPreferenceRepository {
            return SharedPreferenceRepository(context)
        }
    }

    private val sharedPreference = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)

    fun setWalkingHour(time: Int) {
        sharedPreference.edit().putInt(TIME_WALKING_HOUR, time).apply()
    }

    fun setWalkingMinute(time: Int) {
        sharedPreference.edit().putInt(TIME_WALKING_MINUTE, time).apply()
    }

    // 분 단위로 표현
    fun getWalkingTime(): Int {
        return sharedPreference.getInt(TIME_WALKING_MINUTE, 0) + sharedPreference.getInt(
            TIME_WALKING_HOUR,
            0
        ) * 60
    }
}