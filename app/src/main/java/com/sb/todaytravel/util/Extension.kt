package com.sb.todaytravel.util

import com.naver.maps.geometry.LatLng
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun haversine(start: LatLng, destination: LatLng): Double {
    val dLat = Math.toRadians(destination.latitude - start.latitude)
    val dLon = Math.toRadians(destination.longitude - start.longitude)
    val originLat = Math.toRadians(start.latitude)
    val destinationLat = Math.toRadians(destination.latitude)

    val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
    val c = 2 * asin(sqrt(a))
    return EARTH_RADIUS * c
}

const val EARTH_RADIUS = 6372800