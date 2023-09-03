package com.sb.todaytravel.feature.core

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.feature.theme.TodayTravelTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), OnRequestPermissionsResultCallback {
    private val REQUEST_LOCATION = 0
    private val REQUEST_BACKGROUND_LOCATION = 1
    private val REQUEST_NOTIFICATION = 2
    private val REQUESTING_LOCATION_UPDATES_KEY = "requesting_location_updates_key"
    private var requestingLocationUpdates = false

    @Inject
    lateinit var appDataStore: AppDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) return@addOnSuccessListener

                lifecycleScope.launch(Dispatchers.IO) {
                    appDataStore.setCurrentLocationLatitude(location.latitude.toFloat())
                    appDataStore.setCurrentLocationLongitude(location.longitude.toFloat())
                }
            }
        }

        setContent {
           TodayTravelTheme {
               MainScreen(isSystemInDarkTheme())
           }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION)
                }
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location == null) return@addOnSuccessListener

                    lifecycleScope.launch(Dispatchers.IO) {
                        appDataStore.setCurrentLocationLatitude(location.latitude.toFloat())
                        appDataStore.setCurrentLocationLongitude(location.longitude.toFloat())
                    }
                }
            }
            REQUEST_BACKGROUND_LOCATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION)
                    return
                }
            }
            else -> {}
        }
    }
}