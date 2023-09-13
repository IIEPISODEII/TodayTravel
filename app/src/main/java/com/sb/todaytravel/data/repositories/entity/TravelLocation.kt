package com.sb.todaytravel.data.repositories.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "travel_location",
    primaryKeys = ["travelIndex", "arrivalTime"],
    foreignKeys = [ForeignKey(
        entity = TravelHistory::class,
        parentColumns = ["travelIndex"],
        childColumns = ["travelIndex"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TravelLocation(
    var travelIndex: Long = 0L,
    var arrivalTime: Long = 0L,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)
