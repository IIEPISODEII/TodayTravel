package com.sb.todaytravel.ui

import android.graphics.PointF
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.sb.todaytravel.ui.core.BottomNavigationBar
import com.sb.todaytravel.ui.history.HistoryScreen
import com.sb.todaytravel.ui.map.MapScreen
import com.sb.todaytravel.ui.setting.AppSettingScreen

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

        val cameraPosition = rememberCameraPositionState {
            position = CameraPosition(seoul, 11.0)
        }

        LaunchedEffect(key1 = markedTravelLocations, block = {
            if (markedTravelLocations.value.isEmpty()) return@LaunchedEffect
            cameraPosition.position = CameraPosition(
                LatLng(
                    (markedTravelLocations.value.first().latitude + markedTravelLocations.value.last().latitude).toDouble()/2,
                    (markedTravelLocations.value.first().longitude + markedTravelLocations.value.last().longitude).toDouble()/2
                    ,
                ),
                11.0
            )
        })

        var isDestinationMarkShown by remember {
            mutableStateOf(false)
        }

        val mapUiSettings by remember {
            mutableStateOf(
                MapUiSettings(
                    isLocationButtonEnabled = true
                )
            )
        }

        val mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    maxZoom = 15.0,
                    minZoom = 5.0,
                    isNightModeEnabled = isNightMode
                )
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
            SetTravelDestinationDialog(
                onDismissRequest = { isSetDestinationDialogShown = false },
                onAccept = {
                    viewModel.startTravel()
                    isSetDestinationDialogShown = false
                }
            )
        }
        if (isCancelTravelingDialogShown) {
            TravelCancelDialog(
                onDismissRequest = { isCancelTravelingDialogShown = false },
                onAccept = {
                    viewModel.cancelTravel()
                    isCancelTravelingDialogShown = false
                }
            )
        }
        if (!isTraveling.value) IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            onClick = {
                viewModel.setRandomDestination()
                isSetDestinationDialogShown = true
                      },
            enabled = true,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = ""
            )
        }
        if (isTraveling.value) IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            onClick = {
                isCancelTravelingDialogShown = true
                      },
            enabled = true,
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = ""
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
    }
}

@Composable
fun SetTravelDestinationDialog(
    onDismissRequest: () -> Unit = {},
    onAccept: () -> Unit = {}
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1F)
                .wrapContentHeight()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.set_here_destination),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier
                .fillMaxWidth(0.9F)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomStart = 20.dp))
                        .clickable {
                            onDismissRequest()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(R.string.dismiss),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .weight(0.01F)
                )
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomEnd = 20.dp))
                        .clickable {
                            onAccept()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(R.string.accept),
                    fontSize = 14.sp,
                    color = Color(0xFF4D88FF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TravelCancelDialog(
    onDismissRequest: () -> Unit = {},
    onAccept: () -> Unit = {}
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1F)
                .wrapContentHeight()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.question_travel_cancel),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier
                .fillMaxWidth(0.9F)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomStart = 20.dp))
                        .clickable {
                            onDismissRequest()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(id = R.string.dismiss),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .weight(0.01F)
                )
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomEnd = 20.dp))
                        .clickable {
                            onAccept()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(R.string.accept),
                    fontSize = 14.sp,
                    color = Color(0xFF4D88FF),
                    textAlign = TextAlign.Center
                )
            }
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


