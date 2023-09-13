package com.sb.todaytravel.data.repositories.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sb.todaytravel.data.repositories.entity.TravelLocation

@Dao
interface TravelLocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTravelLocation(travelLocation: TravelLocation)

    @Query("SELECT * FROM travel_location WHERE travelIndex = :travelHistoryIndex")
    fun selectTravelLocations(travelHistoryIndex: Long): List<TravelLocation>

    @Delete
    fun deleteTravelLocations(travelLocation: TravelLocation)

    @Query("DELETE FROM travel_location")
    fun deleteAllTravelLocations()
}