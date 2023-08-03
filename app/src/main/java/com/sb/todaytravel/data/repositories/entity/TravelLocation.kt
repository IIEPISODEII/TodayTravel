package com.sb.todaytravel.data.repositories.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "travel_location",
    primaryKeys = ["index", "arrivalTime"],
    foreignKeys = [ForeignKey(
        entity = TravelHistory::class,
        parentColumns = ["index"],
        childColumns = ["index"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TravelLocation(
    var index: Int = 0,
    var arrivalTime: Long = 0L,
    var latitude: Float = 0F,
    var longitude: Float = 0F
)
