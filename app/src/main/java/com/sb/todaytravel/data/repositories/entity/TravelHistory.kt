package com.sb.todaytravel.data.repositories.entity

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @property travelIndex
 * @property travelStartTime
 * @property travelState indicates state of travel result. 0 for completion of travel, 1 for failure of travel(Travel is end, but not at intended destination), 2 for progressing travel.
 */
@Entity(tableName = "history")
data class TravelHistory(
    @PrimaryKey(autoGenerate = true) val travelIndex: Long = 0L,
    var travelStartTime: Long = 0L,
    @IntRange(0, 2) var travelState: Int = 0
)