package com.sb.todaytravel.feature.core

import android.graphics.PointF
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationSource
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.travel_history.HistoryScreen
import com.sb.todaytravel.feature.map.MapScreen
import com.sb.todaytravel.feature.app_setting.AppSettingScreen
import com.sb.todaytravel.feature.theme.TodayTravelTeal

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MainScreen(
    isNightMode: Boolean = true,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val seoul = LatLng(126.986, 37.541)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val markerState by remember {
            mutableStateOf(MarkerState(LatLng(0.toDouble(), 0.toDouble())))
        }
        val currentLocation = viewModel.currentLocation.collectAsState()

        val isTraveling = viewModel.isTraveling.collectAsState()

        val markedTravelLocations = viewModel.markedLocations.collectAsState()

        val preventionOfMapRotation by viewModel.preventionOfMapRotation.collectAsState()

        val cameraPosition = rememberCameraPositionState {
            position = CameraPosition(seoul, 11.0)
        }

        var isDestinationMarkShown by remember {
            mutableStateOf(false)
        }

        println(">> preventionOfMapRotation : $preventionOfMapRotation")

        var mapUiSettings by remember {
            mutableStateOf(
                MapUiSettings(
                    isLocationButtonEnabled = true,
                    isRotateGesturesEnabled = !preventionOfMapRotation
                )
            )
        }

        var mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    maxZoom = 15.0,
                    minZoom = 5.0,
                    isNightModeEnabled = isNightMode
                )
            )
        }

        LaunchedEffect(key1 = preventionOfMapRotation) {
            mapUiSettings = mapUiSettings.copy(
                isRotateGesturesEnabled = !preventionOfMapRotation
            )
        }

        LaunchedEffect(key1 = markedTravelLocations) {
            if (markedTravelLocations.value.isEmpty()) return@LaunchedEffect
            cameraPosition.position = CameraPosition(
                LatLng(
                    (markedTravelLocations.value.first().latitude + markedTravelLocations.value.last().latitude).toDouble() / 2,
                    (markedTravelLocations.value.first().longitude + markedTravelLocations.value.last().longitude).toDouble() / 2,
                ),
                11.0
            )
        }

        LaunchedEffect(key1 = viewModel.destinationLatLng) {
            cameraPosition.position = CameraPosition(
                LatLng(
                    viewModel.destinationLatLng.latitude,
                    viewModel.destinationLatLng.longitude
                ),
                11.0
            )
        }

        val locationSource = remember { CustomLocationSource() }
        var isSetDestinationDialogShown by remember { mutableStateOf(false) }
        var isCancelTravelingDialogShown by remember { mutableStateOf(false) }
        NaverMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapClick = { _, _ -> },
            onMapLongClick = { pointF, coord ->
                isDestinationMarkShown = !isDestinationMarkShown
                cameraPosition.position = CameraPosition(LatLng(coord.latitude, coord.longitude), 10.toDouble())
                             },
            onLocationChange = { location ->
                viewModel.updateCurrentLatLng(LatLng(location))
            },
            locationSource = locationSource
        ) {
            CircleOverlay(
                center = currentLocation.value
            )
            Marker(
                state = markerState,
                visible = isDestinationMarkShown,
                onClick = {
                    isSetDestinationDialogShown = true
                    true
                },
                iconTintColor = Color(0xFF4CAF50)
            )

            markedTravelLocations.value.forEach {
                CircleOverlay(
                    center = LatLng(it.latitude.toDouble(), it.longitude.toDouble()),
                    color = Color(0xFFFFEEEE),
                    radius = 4.toDouble(),
                    outlineColor = Color.Blue,
                    outlineWidth = 2.dp
                )
            }
        }
        if (isSetDestinationDialogShown) {
            AcceptionDialog(
                question = stringResource(R.string.set_here_destination),
                onDismissRequest = { isSetDestinationDialogShown = false },
                onAccept = {
                    viewModel.startTravel()
                    isSetDestinationDialogShown = false
                }
            )
        }
        if (isCancelTravelingDialogShown) {
            AcceptionDialog(
                question = stringResource(R.string.question_travel_cancel),
                onDismissRequest = { isCancelTravelingDialogShown = false },
                onAccept = {
                    viewModel.cancelTravel()
                    isCancelTravelingDialogShown = false
                }
            )
        }

        BottomAnimatedDialog(
            modifier = Modifier
                .fillMaxWidth(0.7F)
                .wrapContentHeight()
                .background(Color.White),
            isShown = rememberNavController().currentBackStackEntry?.destination?.route == Screen.MapScreen.route && isSetDestinationDialogShown,
            onDismiss = { isSetDestinationDialogShown = false },
            onAccept = { viewModel.startTravel() }
        )

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
                    MapScreen()
                }
                composable(Screen.HistoryScreen.route) {
                    HistoryScreen()
                }
                composable(Screen.AppSettingScreen.route) {
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
                if (!isTraveling.value) {
                    viewModel.setRandomDestination()
                    isSetDestinationDialogShown = true
                } else {
                    isCancelTravelingDialogShown = true
                }
            }
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                imageVector = if (isTraveling.value) Icons.Default.Clear else Icons.Default.Add,
                contentDescription = ""
            )
        }
    }
}


private class CustomLocationSource : LocationSource {
    private var listener: LocationSource.OnLocationChangedListener? = null

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        this.listener = listener
    }

    override fun deactivate() {
        listener = null
    }

    fun onMapClick(point: PointF, coord: LatLng) {
        listener?.onLocationChanged(
            Location("CustomLocationSource").apply {
                latitude = coord.latitude
                longitude = coord.longitude
                accuracy = 100f
            }
        )
    }
}


