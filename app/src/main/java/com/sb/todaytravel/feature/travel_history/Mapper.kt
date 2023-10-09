package com.sb.todaytravel.feature.travel_history

import com.sb.todaytravel.data.TravelRecord

fun TravelRecord.toTravelHistoryWithLocations(): TravelHistoryWithLocations {
    return TravelHistoryWithLocations(
        travelHistoryIndex = this.history.travelIndex,
        travelState = this.history.travelState,
        travelStartTime = this.history.travelStartTime,
        travelLocations = this.locations
    )
}