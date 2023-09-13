package com.sb.todaytravel.feature.travel_history

import com.sb.todaytravel.data.repositories.entity.TravelLocation

data class TravelHistoryWithLocations(
    val travelHistoryIndex: Long,
    val travelState: Int,
    val travelStartTime: Long,
    val travelLocations: List<TravelLocation>
)