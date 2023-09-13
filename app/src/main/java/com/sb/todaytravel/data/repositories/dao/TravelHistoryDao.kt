package com.sb.todaytravel.data.repositories.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelHistoryDao {
    @Upsert
    fun insertTravelHistory(travelHistory: TravelHistory): Long

    @Query("SELECT * FROM history")
    fun selectAllTravelHistory(): Flow<List<TravelHistory>>

    @Query("SELECT * FROM history ORDER BY travelIndex DESC")
    fun selectLatestTravelHistory(): TravelHistory

    @Query("SELECT * FROM history ORDER BY travelIndex DESC")
    fun selectLatestTravelHistoryAsFlow(): Flow<TravelHistory?>

    @Query("DELETE FROM history WHERE travelIndex = :deleteIndex")
    fun deleteTravelHistory(deleteIndex: Long)

    @Query("DELETE FROM history")
    suspend fun deleteAllTravelHistories()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTravelLocation(travelLocation: TravelLocation)

    @Transaction
    suspend fun insertTravelHistoryWithLocation(travelHistory: TravelHistory, travelLocation: TravelLocation) {
        insertTravelHistory(travelHistory)
        val latestTravelIdx = selectLatestTravelHistory().travelIndex
        val travelLocationWithId = travelLocation.copy(travelIndex = latestTravelIdx)
        insertTravelLocation(travelLocationWithId)
    }
}