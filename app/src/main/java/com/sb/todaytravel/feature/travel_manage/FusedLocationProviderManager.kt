package com.sb.todaytravel.feature.travel_manage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

object FusedLocationProviderManager {
    private var fusedLocationProviderClientInstance: FusedLocationProviderClient? = null

    fun init(context: Context) {
        fusedLocationProviderClientInstance ?: synchronized(this) {
            fusedLocationProviderClientInstance ?: LocationServices.getFusedLocationProviderClient(context).also { fusedLocationProviderClientInstance = it }
        }
    }

    fun terminate() {
        fusedLocationProviderClientInstance = null
    }

    private var mLocationCallback: LocationCallback? = null

    fun registerLocationCallback(context: Context, locationRequest: LocationRequest, locationCallback: LocationCallback) {
        mLocationCallback ?: return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationProviderClientInstance?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        mLocationCallback = locationCallback
    }

    fun unregisterLocationCallback() {
        if (mLocationCallback == null) return

        fusedLocationProviderClientInstance?.removeLocationUpdates(mLocationCallback!!)
        mLocationCallback = null
    }
}