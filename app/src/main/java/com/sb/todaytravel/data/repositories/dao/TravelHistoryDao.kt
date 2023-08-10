package com.sb.todaytravel.data.repositories.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTravelHistory(travelHistory: TravelHistory)

    @Query("SELECT * FROM history")
    fun selectAllTravelHistory(): Flow<List<TravelHistory>>

    @Query("SELECT * FROM history ORDER BY `index` DESC")
    fun selectLatestTravelHistory(): TravelHistory

    @Query("SELECT * FROM history ORDER BY `index` DESC")
    fun selectLatestTravelHistoryAsFlow(): Flow<TravelHistory?>

    @Query("DELETE FROM history WHERE `index` = :deleteIndex")
    fun deleteTravelHistory(deleteIndex: Int)

    @Query("DELETE FROM history")
    fun deleteAllTravelHistories()
}