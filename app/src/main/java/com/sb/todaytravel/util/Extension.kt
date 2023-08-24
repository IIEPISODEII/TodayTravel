package com.sb.todaytravel.util

import com.naver.maps.geometry.LatLng

fun haversine(start: LatLng, destination: LatLng): Double {
    val dLat = Math.toRadians(destination.latitude - start.latitude);
    val dLon = Math.toRadians(destination.longitude - start.longitude);
    val originLat = Math.toRadians(start.latitude);
    val destinationLat = Math.toRadians(destination.latitude);

    val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(Math.sin(dLon / 2), 2.toDouble()) * Math.cos(originLat) * Math.cos(destinationLat);
    val c = 2 * Math.asin(Math.sqrt(a));
    return EARTH_RADIUS * c;
}

const val EARTH_RADIUS = 6372.8