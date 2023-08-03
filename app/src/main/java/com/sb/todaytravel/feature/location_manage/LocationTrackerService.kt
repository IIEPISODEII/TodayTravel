package com.sb.todaytravel.feature.location_manage

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sb.todaytravel.data.repositories.AppDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Track user's current location in background.
 * Location permission should be followed to start this service.
 * This service starts when user starts traveling. And this will be terminated if user cancels traveling.
 */
class LocationTrackerService: Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val appDataStore = AppDataStore(applicationContext)
        coroutineScope = CoroutineScope(Dispatchers.IO)

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                coroutineScope.launch {
                    appDataStore.setCurrentLocationLatitude(locationResult.locations.last().latitude.toFloat())
                    appDataStore.setCurrentLocationLongitude(locationResult.locations.last().longitude.toFloat())
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}