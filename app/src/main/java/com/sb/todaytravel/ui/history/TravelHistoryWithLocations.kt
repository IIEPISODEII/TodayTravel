package com.sb.todaytravel.ui.history

import com.sb.todaytravel.data.repositories.entity.TravelLocation

data class TravelHistoryWithLocations(
    val travelHistoryIndex: Int,
    val travelStartTime: Long,
    val travelLocations: List<TravelLocation>
)