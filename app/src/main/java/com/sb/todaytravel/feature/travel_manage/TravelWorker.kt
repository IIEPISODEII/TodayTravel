package com.sb.todaytravel.feature.travel_manage

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.naver.maps.geometry.LatLng
import com.sb.todaytravel.R
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.core.INIT_LATITUDE
import com.sb.todaytravel.feature.core.INIT_LONGITUDE
import com.sb.todaytravel.feature.core.MainActivity
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import com.sb.todaytravel.util.haversine
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

    private var latitude = INIT_LATITUDE.toFloat()
    private var longitude = INIT_LONGITUDE.toFloat()

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

        delay(1000L)

        val destination = inputData.getDoubleArray(TRAVEL_DESTINATION)

        val latestTravelHistory: TravelHistory
        try {
            latestTravelHistory = AppDatabase.getInstance(appContext).getTravelHistoryDao().selectLatestTravelHistory()

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

        if ((destination?.get(0) ?: INIT_LATITUDE) - latitude < 0.00001 && (destination?.get(1) ?: INIT_LONGITUDE) - longitude < 0.00001) {
            sendNotification(1, latestTravelHistory.travelStartTime)
            appDataStore.setCurrentTravelWorkerId("")
            AppDatabase
                .getInstance(appContext)
                .getTravelLocationDao()
                .insertTravelLocation(
                    TravelLocation(
                        index = latestTravelHistory.index,
                        arrivalTime = System.currentTimeMillis(),
                        latitude = destination?.get(0)?.toFloat() ?: 37F,
                        longitude = destination?.get(1)?.toFloat() ?: 127F,
                    )
                )
            return Result.success()
        }

        if (isStopped) return Result.success()

        val continuousTravelWorkRequest = OneTimeWorkRequestBuilder<TravelWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(appContext)
            .enqueue(continuousTravelWorkRequest)

        return Result.success()
    }

    private fun sendNotification(id: Int, travelStartTime: Long) {
        val travelTime = (System.currentTimeMillis() - travelStartTime) / 1000 / 60

        val intent = Intent(appContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager = NotificationManagerCompat.from(appContext)

        val titleNotification = "목적지 도착!"
        val subtitleNotification = "${travelTime}분만에 도착했어요."
        val pendingIntent = getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat
            .Builder(appContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_flag_24)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(Notification.DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        createNotificationChannel()

        notificationManager.notify(id, notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        enableLights(true)
                        lightColor = TodayTravelTeal.toArgb()
                        description = "도착했어요!"
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TRAVEL_DESTINATION = "destination"
        const val NOTIFICATION_ID = "today_travel_notification"
        const val NOTIFICATION_CHANNEL = "today_travel_noti_channel"
        const val NOTIFICATION_NAME = "목적지 도착 알람"
    }
}