package com.sb.todaytravel.feature.travel_manage

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.impl.model.Dependency
import androidx.work.workDataOf
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class TravelWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val appDataStore: AppDataStore
): CoroutineWorker(appContext, workerParams) {

    private var latitude = 0F
    private var longitude = 0F

    private val coroutineScopeA = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeB = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        coroutineScopeA.launch {
            appDataStore.getCurrentLocationLatitude().collect {
                latitude = it
            }
        }
        coroutineScopeB.launch {
            appDataStore.getCurrentLocationLongitude().collect {
                longitude = it
            }
        }
        delay(33L)

        try {
            val latestTravelHistory = AppDatabase.getInstance(appContext).getTravelHistoryDao().selectLatestTravelHistory()

            val newTravelLocation = TravelLocation(
                index = latestTravelHistory.index,
                arrivalTime = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude
            )

            AppDatabase.getInstance(appContext).getTravelLocationDao().insertTravelLocation(newTravelLocation)
            coroutineScopeA.cancel()
            coroutineScopeB.cancel()
        } catch(e: Exception) {
            e.printStackTrace()
            coroutineScopeA.cancel()
            coroutineScopeB.cancel()
            return Result.failure()
        }

        return Result.success()
    }
}