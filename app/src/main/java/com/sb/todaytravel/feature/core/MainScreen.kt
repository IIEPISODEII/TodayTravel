package com.sb.todaytravel.feature.core

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PathOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.app_setting.AppSettingScreen
import com.sb.todaytravel.feature.map.MapScreen
import com.sb.todaytravel.feature.map.rememberFusedLocationSource
import com.sb.todaytravel.feature.theme.TodayTravelBlue
import com.sb.todaytravel.feature.theme.TodayTravelGreen
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import com.sb.todaytravel.feature.travel_history.HistoryScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MainScreen(
    isNightMode: Boolean = true,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val seoul = LatLng(INIT_LATITUDE, INIT_LONGITUDE)
    val coroutineScope = rememberCoroutineScope()
    var dialogEvent by remember { mutableStateOf(DialogEvent.NONE) }
    val currentLocation by mainViewModel.currentLocation.collectAsState()
    val isTraveling by mainViewModel.isTraveling.collectAsState()
    val markedTravelLocations by mainViewModel.markedLocations.collectAsState()
    val preventionOfMapRotation by mainViewModel.preventionOfMapRotation.collectAsState()
    val destination by mainViewModel.destination.collectAsState()
    val cameraPosition = rememberCameraPositionState {
        position = CameraPosition(seoul, 11.0)
    }

    LaunchedEffect(key1 = Unit) {
        delay(1500L)
        cameraPosition.position = CameraPosition(currentLocation, 11.0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val destinationCandidateMarkerState by remember {
            mutableStateOf(MarkerState(LatLng(0.toDouble(), 0.toDouble())))
        }
        var mapUiSettings by remember {
            mutableStateOf(
                MapUiSettings(
                    isLocationButtonEnabled = true,
                    isRotateGesturesEnabled = !preventionOfMapRotation
                )
            )
        }
        val mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    isNightModeEnabled = isNightMode,
                    locationTrackingMode = LocationTrackingMode.NoFollow
                )
            )
        }
        var isDestinationMarkShown by remember { mutableStateOf(false) }
        var fullScreenMapEnabled by remember { mutableStateOf(false) }
        val isLoading by mainViewModel.isLoading.collectAsState()

        LaunchedEffect(key1 = preventionOfMapRotation) {
            mapUiSettings = mapUiSettings.copy(
                isRotateGesturesEnabled = !preventionOfMapRotation
            )
        }

        LaunchedEffect(key1 = markedTravelLocations) {
            if (markedTravelLocations.isEmpty()) return@LaunchedEffect
            cameraPosition.position = CameraPosition(
                LatLng(
                    (markedTravelLocations.first().latitude + markedTravelLocations.last().latitude).toDouble() / 2,
                    (markedTravelLocations.first().longitude + markedTravelLocations.last().longitude).toDouble() / 2,
                ),
                15.0
            )
        }

        LaunchedEffect(key1 = mainViewModel.candidateDestination) {
            destinationCandidateMarkerState.position = mainViewModel.candidateDestination
            cameraPosition.animate(
                update = CameraUpdate.toCameraPosition(CameraPosition(
                    LatLng(
                        mainViewModel.candidateDestination.latitude,
                        mainViewModel.candidateDestination.longitude
                    ),
                    15.0
                )),
                animation = CameraAnimation.Easing,
                durationMs = 200
            )
        }
        NaverMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (fullScreenMapEnabled) 1F else 0.49F)
                .align(Alignment.TopEnd)
                .padding(bottom = 60.dp),
            uiSettings = mapUiSettings,
            properties = mapProperties,
            cameraPositionState = cameraPosition,
            onMapClick = { _, _ -> },
            onMapLongClick = { pointF, coord ->
                isDestinationMarkShown = true
                destinationCandidateMarkerState.position = coord
                coroutineScope.launch {
                    cameraPosition.animate(
                        update = CameraUpdate.toCameraPosition(CameraPosition(LatLng(coord.latitude, coord.longitude), 15.toDouble())),
                        animation = CameraAnimation.Easing,
                        durationMs = 200
                    )
                }
                             },
            onLocationChange = { location ->
                mainViewModel.updateCurrentLatLng(LatLng(location))
            },
            locationSource = rememberFusedLocationSource()
        ) {
            Marker(
                state = destinationCandidateMarkerState,
                visible = isDestinationMarkShown && !isTraveling,
                onClick = {
                    dialogEvent = DialogEvent.SET_DESTINATION
                    mainViewModel.setDestination(destinationCandidateMarkerState.position)
                    true
                },
                iconTintColor = TodayTravelTeal,
                captionText = "여기를 도착지점으로",
                isFlat = true
            )

            Marker(
                state = MarkerState(position = destination),
                visible = isTraveling,
                iconTintColor = TodayTravelBlue,
                captionText = "도착지점",
                isFlat = true
            )

            markedTravelLocations.forEach { (latitude, longitude) ->
                CircleOverlay(
                    center = LatLng(latitude.toDouble(), longitude.toDouble()),
                    color = Color.Transparent,
                    radius = 12.toDouble(),
                    outlineColor = TodayTravelGreen,
                    outlineWidth = 2.dp
                )
            }
            if (markedTravelLocations.size >= 2) {
                PathOverlay(
                    coords = markedTravelLocations.map { LatLng(it.latitude.toDouble(), it.longitude.toDouble()) },
                    width = 4.dp,
                    color = TodayTravelGreen
                )
            }
        }


        if (dialogEvent != DialogEvent.NONE) {
            AcceptionDialog(
                question = when (dialogEvent) {
                    DialogEvent.SET_DESTINATION -> stringResource(R.string.set_here_destination)
                    DialogEvent.CANCEL_TRAVEL -> stringResource(id = R.string.question_travel_cancel)
                    else -> ""
                },
                onDismissRequest = {
                    dialogEvent = DialogEvent.NONE
                },
                onAccept = {
                    when (dialogEvent) {
                        DialogEvent.SET_DESTINATION -> if (!isLoading) mainViewModel.startTravel()
                        DialogEvent.CANCEL_TRAVEL -> mainViewModel.cancelTravel()
                        else -> {}
                    }
                    dialogEvent = DialogEvent.NONE
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
                navController = navController,
                startDestination = Screen.MapScreen.route
            ) {
                composable(Screen.MapScreen.route) {
                    fullScreenMapEnabled = true
                    MapScreen()
                }
                composable(
                    Screen.HistoryScreen.route,
                    enterTransition = { slideInVertically(initialOffsetY = { it }) },
                    exitTransition = { slideOutVertically(targetOffsetY = { it }) }
                ) {
                    fullScreenMapEnabled = false
                    HistoryScreen(onHistoryItemClick = { mainViewModel.updateMarkedTravelHistory(it) })
                }
                composable(Screen.AppSettingScreen.route) {
                    fullScreenMapEnabled = true
                    AppSettingScreen()
                }
            }
            BottomNavigationBar(navController = navController)
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 84.dp),
            backgroundColor = TodayTravelTeal,
            onClick = {
                if (!isTraveling) {
                    mainViewModel.setRandomDestination()
                    isDestinationMarkShown = true
                    dialogEvent = DialogEvent.SET_DESTINATION
                } else {
                    dialogEvent = DialogEvent.CANCEL_TRAVEL
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                imageVector = if (isTraveling) Icons.Default.Clear else Icons.Default.Add,
                contentDescription = ""
            )
        }
    }
}


enum class DialogEvent {
    NONE,
    CANCEL_TRAVEL,
    SET_DESTINATION
}

