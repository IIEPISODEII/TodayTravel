package com.sb.todaytravel.data

import androidx.room.Embedded
import androidx.room.Relation
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation

data class TravelRecord(
    @Embedded
    val history: TravelHistory,

    @Relation(parentColumn = "travelIndex", entityColumn = "travelIndex")
    val locations: List<TravelLocation>
) {
    override fun toString() = "travelIndex: " + history.travelIndex + ", " + "travelState: " + history.travelState + ": " + locations.toString()
}
