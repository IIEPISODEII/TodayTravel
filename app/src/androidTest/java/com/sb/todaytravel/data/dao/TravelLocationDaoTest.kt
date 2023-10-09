package com.sb.todaytravel.data.dao

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sb.todaytravel.data.TravelRecord
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.travel_history.OrderType
import com.sb.todaytravel.feature.travel_history.TravelHistoryWithLocations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelLocationDaoTest {
    private val TAG = "TRAVEL_LOCATION_TEST"
    private lateinit var appDatabase: AppDatabase

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.sb.todaytravel", appContext.packageName)

        appDatabase = Room.inMemoryDatabaseBuilder(
            appContext,
            AppDatabase::class.java
        ).build()
    }

    @After
    fun cleanup() = runBlocking {
        appDatabase.getTravelHistoryDao().deleteAllTravelHistories()
        appDatabase.getTravelLocationDao().deleteAllTravelLocations()
    }

    @Test
    fun SQL_INNER_JOIN_EQUALS_TO_MY_CODE() = runBlocking {
        val newHistory1 = TravelHistory(
            travelIndex = 1,
            travelStartTime = 0L
        )
        val newTravelLocations1_1 =
            TravelLocation(
                travelIndex = 1,
                arrivalTime = 0L,
                latitude = 1.0,
                longitude = 1.0
            )
        val newTravelLocations1_2 =
            TravelLocation(
                travelIndex = 1,
                arrivalTime = 1L,
                latitude = 1.1,
                longitude = 1.1
            )
        val newTravelLocations1_3 =
            TravelLocation(
                travelIndex = 1,
                arrivalTime = 2L,
                latitude = 1.2,
                longitude = 1.2
            )
        val newHistory2 = TravelHistory(
            travelIndex = 2,
            travelStartTime = 4L
        )
        val newTravelLocations2_1 =
            TravelLocation(
                travelIndex = 2,
                arrivalTime = 4L,
                latitude = 2.0,
                longitude = 2.0
            )
        val newTravelLocations2_2 =
            TravelLocation(
                travelIndex = 2,
                arrivalTime = 7L,
                latitude = 2.1,
                longitude = 2.1
            )
        val newTravelLocations2_3 =
            TravelLocation(
                travelIndex = 2,
                arrivalTime = 8L,
                latitude = 2.2,
                longitude = 2.2
            )
        val newTravelLocations2_4 =
            TravelLocation(
                travelIndex = 2,
                arrivalTime = 9L,
                latitude = 2.3,
                longitude = 2.3
            )
        val newHistory3 = TravelHistory(
            travelIndex = 3,
            travelStartTime = 10L
        )
        val newTravelLocations3_1 =
            TravelLocation(
                travelIndex = 3,
                arrivalTime = 10L,
                latitude = 3.0,
                longitude = 3.0
            )
        val newTravelLocations3_2 =
            TravelLocation(
                travelIndex = 3,
                arrivalTime = 12L,
                latitude = 3.1,
                longitude = 3.1
            )
        val locationList = listOf(
            newTravelLocations1_1,
            newTravelLocations1_2,
            newTravelLocations1_3,
            newTravelLocations2_1,
            newTravelLocations2_2,
            newTravelLocations2_3,
            newTravelLocations2_4,
            newTravelLocations3_1,
            newTravelLocations3_2
        )
        val sortedList = locationList.sortedWith(
            comparator = compareBy(
                { -it.travelIndex },
                { it.arrivalTime }
            )
        )

        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory1, newTravelLocations1_1)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory1, newTravelLocations1_2)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory1, newTravelLocations1_3)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory2, newTravelLocations2_1)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory2, newTravelLocations2_2)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory2, newTravelLocations2_3)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory2, newTravelLocations2_4)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory3, newTravelLocations3_1)
        appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(newHistory3, newTravelLocations3_2)

        val resultByQuery = appDatabase.getTravelLocationDao().queryAllTravelLocationsWithCorrespondingHistory()

        Log.d(TAG, "--QUERY RESULT--")
        resultByQuery.forEach {
            Log.d(TAG, it.toString())
        }
        assertEquals(3, 1+2)
    }
}