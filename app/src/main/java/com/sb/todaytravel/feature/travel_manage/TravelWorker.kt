package com.sb.todaytravel.feature.travel_manage

import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class TravelWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDataStore: AppDataStore
): CoroutineWorker(appContext, workerParams) {

    private var latitude = 0F
    private var longitude = 0F

    private val coroutineScopeA = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeB = CoroutineScope(Dispatchers.IO)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var coroutineScope: CoroutineScope

    override suspend fun doWork(): Result {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
        coroutineScope = CoroutineScope(Dispatchers.IO)

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                coroutineScope.launch {
                    appDataStore.setCurrentLocationLatitude(locationResult.locations.last().latitude.toFloat())
                    appDataStore.setCurrentLocationLongitude(locationResult.locations.last().longitude.toFloat())
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(appContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest
                .Builder(5*60*1000L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

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

        if (isStopped) return Result.success()

        val continuousTravelWorkRequest = OneTimeWorkRequestBuilder<TravelWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(appContext)
            .enqueue(continuousTravelWorkRequest)

        return Result.success()
    }
}