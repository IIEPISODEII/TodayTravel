package com.sb.todaytravel.data.repositories.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sb.todaytravel.data.TravelRecord
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.travel_history.TravelHistoryWithLocations
import kotlinx.coroutines.flow.Flow

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

    @Transaction // Transaction 없을 경우 POJO 간 관계 일관성이 깨질 수 있다는 경고 뜸
    @Query("SELECT * FROM history ORDER BY travelIndex DESC")
    fun selectAllTravelLocationsGrouped(): Flow<List<TravelRecord>>
}