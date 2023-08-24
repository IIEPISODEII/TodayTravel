package com.sb.todaytravel.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naver.maps.geometry.LatLng
import com.sb.todaytravel.data.datasources.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import javax.inject.Inject

class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private val TRAVEL_RADIUS = intPreferencesKey("travel_radius")
    private val ORDER_TYPE = intPreferencesKey("order_type")
    private val CURRENT_TRAVEL_WORKER_ID = stringPreferencesKey("current_travel_worker_id")
    private val CURRENT_LATITUDE = floatPreferencesKey("current_latitude")
    private val CURRENT_LONGITUDE = floatPreferencesKey("current_longitude")
    private val MAP_ROTATION = booleanPreferencesKey("map_rotation")
    private val DESTINATION_LATLNG = stringPreferencesKey("destination_latlng")

    suspend fun setTravelRadius(radius: Int) {
        dataStore.edit { pref ->
            if (!pref.contains(TRAVEL_RADIUS)) pref[TRAVEL_RADIUS] = 0
            pref[TRAVEL_RADIUS] = if (pref[TRAVEL_RADIUS]!! + 100 <= 1000) pref[TRAVEL_RADIUS]!! + 100 else pref[TRAVEL_RADIUS]!!-1000
        }
    }

    fun getTravelRadius(): Flow<Int> {
        return dataStore.data.map { pref ->
            pref[TRAVEL_RADIUS] ?: 0
        }
    }

    suspend fun setOrderType(orderType: Int) {
        dataStore.edit { pref ->
            pref[ORDER_TYPE] = orderType
        }
    }

    fun getOrderType(): Flow<Int> {
        return dataStore.data.map { pref ->
            pref[ORDER_TYPE] ?: ORDER_TYPE_DESCEND
        }
    }

    suspend fun setCurrentTravelWorkerId(id: String) {
        dataStore.edit { pref ->
            pref[CURRENT_TRAVEL_WORKER_ID] = id
        }
    }

    fun getCurrentTravelWorkerId(): Flow<String> = dataStore.data.map { pref -> pref[CURRENT_TRAVEL_WORKER_ID] ?: "" }

    suspend fun setCurrentLocationLatitude(latitude: Float) {
        dataStore.edit { pref ->
            pref[CURRENT_LATITUDE] = latitude
        }
    }

    fun getCurrentLocationLatitude(): Flow<Float> {

        return dataStore.data.map { pref ->
            pref[CURRENT_LATITUDE] ?: 0F
        }
    }

    suspend fun setCurrentLocationLongitude(longitude: Float) {
        dataStore.edit { pref ->
            pref[CURRENT_LONGITUDE] = longitude
        }
    }

    fun getCurrentLocationLongitude(): Flow<Float> {
        return dataStore.data.map { pref ->
            pref[CURRENT_LONGITUDE] ?: 0F
        }
    }

    suspend fun setPreventionOfMapRotation(prevention: Boolean) {
        dataStore.edit { pref ->
            pref[MAP_ROTATION] = prevention
        }
    }

    fun getPreventionOfMapRotation(): Flow<Boolean> {
        return dataStore.data.map { pref ->
            pref[MAP_ROTATION] ?: true
        }
    }

    suspend fun setDestinationLatLng(destination: LatLng) {
        dataStore.edit { pref ->
            pref[DESTINATION_LATLNG] = destination.latitude.toString() + " " + destination.longitude.toString()
        }
    }

    fun getDestinationLatLng(): Flow<LatLng> {
        return dataStore.data.map { pref ->
            pref[DESTINATION_LATLNG] ?: "37.541 126.79141"
        }.map {
            val splitResult = it.split(' ')
            LatLng(splitResult[0].toDouble(), splitResult[1].toDouble())
        }
    }

    companion object {
        const val ORDER_TYPE_ASCEND = 0
        const val ORDER_TYPE_DESCEND = 1
    }
}