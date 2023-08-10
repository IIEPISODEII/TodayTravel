package com.sb.todaytravel.feature.travel_history

import com.sb.todaytravel.data.repositories.entity.TravelLocation

data class TravelHistoryWithLocations(
    val travelHistoryIndex: Int,
    val travelStartTime: Long,
    val travelLocations: List<TravelLocation>
)