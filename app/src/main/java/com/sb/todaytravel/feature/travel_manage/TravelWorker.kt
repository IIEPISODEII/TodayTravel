package com.sb.todaytravel.feature.travel_manage

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sb.todaytravel.R
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.core.INIT_LATITUDE
import com.sb.todaytravel.feature.core.INIT_LONGITUDE
import com.sb.todaytravel.feature.core.MainActivity
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@HiltWorker
class TravelWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appDataStore: AppDataStore
): CoroutineWorker(appContext, workerParams) {

    private val appDatabase = AppDatabase.getInstance(appContext)
    private var latitude = inputData.getDouble(PRE_LOCATION_LATITUDE, INIT_LATITUDE)
    private var longitude = inputData.getDouble(PRE_LOCATION_LONGITUDE, INIT_LONGITUDE)

    private val coroutineScopeA = CoroutineScope(Dispatchers.IO)
    private val coroutineScopeB = CoroutineScope(Dispatchers.IO)

    private lateinit var locationCallback: LocationCallback
    private lateinit var coroutineScope: CoroutineScope

    override suspend fun doWork(): Result {
        try {
            coroutineScope = CoroutineScope(Dispatchers.IO)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    coroutineScope.launch {
                        appDataStore.setCurrentLocationLatitude(locationResult.locations.last().latitude)
                        appDataStore.setCurrentLocationLongitude(locationResult.locations.last().longitude)
                    }
                }
            }

            val locationRequest = LocationRequest
                .Builder(60 * 1000L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            FusedLocationProviderManager.registerLocationCallback(
                context = applicationContext,
                locationRequest = locationRequest,
                locationCallback = locationCallback
            )

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

            val destination = inputData.getDoubleArray(TRAVEL_DESTINATION)
            val destinationLatitude = destination?.get(0) ?: INIT_LATITUDE
            val destinationLongitude = destination?.get(1) ?: INIT_LONGITUDE
            val isTravelEndAtDestination = sqrt(
                abs(destinationLatitude - latitude).pow(2) + abs(destinationLongitude - longitude).pow(2)) < 0.0005

            delay(1000L)

            var latestHistoryIndex = inputData.getLong(TRAVEL_INDEX, -1)

            try {
                val newTravelLocation = TravelLocation(
                    travelIndex = latestHistoryIndex,
                    arrivalTime = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude
                )
                if (latestHistoryIndex == -1L) { // TravelHistory 신규 생성 후 TravelHistory와 TravelLocation을 같이 DB에 삽입
                    val newTravelHistory = TravelHistory(
                        travelStartTime = System.currentTimeMillis(),
                        travelState = 2
                    )

                    latestHistoryIndex = withContext(Dispatchers.IO) {
                        appDatabase.getTravelHistoryDao()
                            .insertTravelHistoryWithLocation(newTravelHistory, newTravelLocation)
                        appDatabase.getTravelHistoryDao().selectLatestTravelHistory().travelIndex
                    }
                } else { // 신규 TravelLocation만 DB에 삽입
                    appDatabase.getTravelLocationDao().insertTravelLocation(newTravelLocation)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FusedLocationProviderManager.unregisterLocationCallback()
                return Result.failure()
            }

            if (isStopped) {
                FusedLocationProviderManager.unregisterLocationCallback()
                return Result.success()
            }

            if (isTravelEndAtDestination) {
                sendNotification(1, System.currentTimeMillis())
                appDataStore.setCurrentTravelWorkerId("")
                val latestTravelHistory = withContext(Dispatchers.IO) {
                    appDatabase
                        .getTravelHistoryDao()
                        .selectLatestTravelHistory()
                }
                appDatabase
                    .getTravelHistoryDao()
                    .insertTravelHistoryWithLocation(
                        travelHistory = latestTravelHistory.copy(travelState = 0),
                        travelLocation = TravelLocation(
                            travelIndex = latestHistoryIndex,
                            arrivalTime = System.currentTimeMillis(),
                            latitude = destinationLatitude,
                            longitude = destinationLongitude
                        )
                    )
                FusedLocationProviderManager.unregisterLocationCallback()
                return Result.success()
            }

            coroutineScopeA.cancel()
            coroutineScopeB.cancel()

            delay(60000L)

            val continuousTravelWorkRequest = OneTimeWorkRequestBuilder<TravelWorker>()
                .setInputData(
                    workDataOf(
                        Pair(TRAVEL_INDEX, latestHistoryIndex),
                        Pair(PRE_LOCATION_LATITUDE, latitude),
                        Pair(PRE_LOCATION_LONGITUDE, longitude),
                        Pair(
                            TRAVEL_DESTINATION, arrayOf(destinationLatitude, destinationLongitude)
                        )
                    )
                )
                .build()

            WorkManager.getInstance(appContext)
                .enqueue(continuousTravelWorkRequest)

            return Result.success()
        } catch(e: CancellationException) {
            e.printStackTrace()
            FusedLocationProviderManager.unregisterLocationCallback()
            return Result.failure()
        }
    }

    private fun sendNotification(id: Int, travelStartTime: Long) {
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        createNotificationChannel()

        val travelTime = (System.currentTimeMillis() - travelStartTime) / 1000 / 60
        val intent = Intent(appContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(TRAVEL_END_NOTIFICATION_ID, id)

        val notificationManager = NotificationManagerCompat.from(appContext)

        val titleNotification = "목적지 도착!"
        val subtitleNotification = "${travelTime}분만에 도착했어요."
        val pendingIntent = getActivity(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat
            .Builder(appContext, TRAVEL_END_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_flag_24)
            .setContentTitle(titleNotification)
            .setContentText(subtitleNotification)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
        notificationManager.deleteNotificationChannel(TRAVEL_START_NOTIFICATION_CHANNEL)
        notificationManager.cancel(2)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(TRAVEL_END_NOTIFICATION_CHANNEL, TRAVEL_END_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)
                    .apply {
                        enableLights(true)
                        lightColor = TodayTravelTeal.toArgb()
                        description = "도착했어요!"
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val TRAVEL_INDEX = "travel_index"
        const val PRE_LOCATION_LATITUDE = "pre_location_latitude"
        const val PRE_LOCATION_LONGITUDE = "pre_location_longitude"
    }
}