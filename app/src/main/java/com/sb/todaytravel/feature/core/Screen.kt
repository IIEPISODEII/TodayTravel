package com.sb.todaytravel.feature.core

import androidx.annotation.DrawableRes
import com.sb.todaytravel.R

sealed class Screen(val route: String, @DrawableRes val iconResourceId: Int) {
    object MapScreen : Screen(
        route = "map_screen",
        iconResourceId = R.drawable.baseline_location_on_24
    )

    object HistoryScreen : Screen(
        route = "history_screen",
        iconResourceId = R.drawable.baseline_history_24
    )

    object AppSettingScreen : Screen(
        route = "app_setting_screen",
        iconResourceId = R.drawable.baseline_settings_24
    )
}