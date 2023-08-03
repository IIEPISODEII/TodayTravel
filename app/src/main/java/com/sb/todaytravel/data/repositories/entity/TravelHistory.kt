package com.sb.todaytravel.data.repositories.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class TravelHistory(
    @PrimaryKey(autoGenerate = true) val index: Int = 0,
    var travelStartTime: Long = 0L
)