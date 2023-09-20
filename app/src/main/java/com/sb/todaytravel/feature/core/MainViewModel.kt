package com.sb.todaytravel.feature.core

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.naver.maps.geometry.LatLng
import com.sb.todaytravel.R
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import com.sb.todaytravel.feature.travel_manage.TravelWorker
import com.sb.todaytravel.feature.travel_history.TravelHistoryWithLocations
import com.sb.todaytravel.feature.travel_manage.FusedLocationProviderManager
import com.sb.todaytravel.feature.travel_manage.TRAVEL_DESTINATION
import com.sb.todaytravel.feature.travel_manage.TRAVEL_START_NOTIFICATION_CHANNEL
import com.sb.todaytravel.feature.travel_manage.TRAVEL_START_NOTIFICATION_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val appDatabase: AppDatabase,
    private val application: Application
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application.applicationContext)

    private val _currentLocation = MutableStateFlow((LatLng(INIT_LATITUDE, INIT_LONGITUDE)))
    val currentLocation: StateFlow<LatLng>
        get() = _currentLocation.asStateFlow()

    private val _destination = MutableStateFlow((LatLng(0.toDouble(), 0.toDouble())))
    val destination: StateFlow<LatLng>
        get() = _destination.asStateFlow()

    var candidateDestination = LatLng(INIT_LATITUDE, INIT_LONGITUDE)
        private set

    private var travelRadius = 0

    private val _isTraveling = MutableStateFlow(false)
    val isTraveling: StateFlow<Boolean>
        get() = _isTraveling.asStateFlow()

    private val _lastTravelHistoryIndex = MutableStateFlow(-1L)
    val lastTravelHistoryIndex: StateFlow<Long>
        get() = _lastTravelHistoryIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private var lastLocationLatitude = 0.0
    private var lastLocationLongitude = 0.0

    private val _preventionOfMapRotation = MutableStateFlow(true)
    val preventionOfMapRotation: StateFlow<Boolean>
        get() = _preventionOfMapRotation.asStateFlow()

    // 주소 정보 불러오기
    fun updateCoordInfo(query: LatLng) {
        viewModelScope.launch {
//            val apiResponse = getAddressFromNaverUsecase.getAddressFromNaver(query = query)
//            val apiResponseBody = apiResponse.body()
//
//            // 원래는 try catch 써서 404 같은 에러 처리해야 함
//            var numberAddress = StringBuilder("결과 없음")
//            var roadnameAddress = StringBuilder("결과 없음")
//
//            if (apiResponseBody?.status?.code == 0) {
//                val addressBase = apiResponseBody.results[0]?.region
//                // 도,시/구/동/리
//                numberAddress = StringBuilder("${addressBase?.area1?.name} ")
//                    .append("${addressBase?.area2?.name} ")
//                    .append("${addressBase?.area3?.name} ")
//                    .append("${addressBase?.area4?.name} ")
//                if (apiResponseBody.results.lastIndex >= 1) {
//                    // 번지수
//                    numberAddress = numberAddress
//                        .append("${apiResponseBody.results[1]?.land?.number1}")
//                    val apiResponseBodyLandNumber2 = apiResponseBody.results[1]?.land?.number2
//                    // 번지수-번지수 ex) 102-3번지
//                    if (apiResponseBodyLandNumber2 != null && apiResponseBodyLandNumber2.isNotBlank()) {
//                        numberAddress =
//                            numberAddress.append("-${apiResponseBody.results[1]?.land?.number2}")
//                    }
//                    // 길 + 건물번호
//                    if (apiResponseBody.results.lastIndex >= 3) {
//                        val roadAddrBase = apiResponseBody.results[3]?.region
//                        roadnameAddress = StringBuilder("${roadAddrBase?.area1?.name} ")
//                            .append("${roadAddrBase?.area2?.name} ")
//                            .append("${roadAddrBase?.area3?.name} ")
//                        val apiResponseRoadAddrArea4Name = roadAddrBase?.area4?.name
//                        if (apiResponseRoadAddrArea4Name != null && apiResponseRoadAddrArea4Name.isNotBlank()) roadnameAddress =
//                            roadnameAddress.append(roadAddrBase.area4.name)
//                        roadnameAddress = roadnameAddress
//                            .append(apiResponseBody.results[3]?.land?.name)
//                            .append(" ${apiResponseBody.results[3]?.land?.number1}")
//                    }
//                }
//            }
//            _markedCoordInfo.value = CoordInfoData(
//                query.latitude.toDMS(),
//                query.longitude.toDMS(),
//                numberAddress.toString(),
//                roadnameAddress.toString()
//            )
        }
    }

    // 장소 찾기
    fun setRandomDestination() {
        val mLatitude = _currentLocation.value.latitude
        val mLongitude = _currentLocation.value.longitude

        val newLatitude: Double
        val newLongitude: Double

        val distanceX: Float = travelRadius * Math.random().toFloat()
        val randomDistanceX: Float = if (Math.random() >= 0.5) distanceX else -distanceX
        val distanceY: Float = sqrt(travelRadius * travelRadius - distanceX * distanceX)
        val randomDistanceY: Float = if (Math.random() >= 0.5) distanceY else -distanceY

        val coorX = mapDistanceToCoordinate(randomDistanceX)
        val coorY = mapDistanceToCoordinate(randomDistanceY)

        newLatitude = mLatitude + coorY
        newLongitude = mLongitude + coorX

        candidateDestination = LatLng(newLatitude, newLongitude)
    }

    fun setDestination(latLng: LatLng) {
        candidateDestination = latLng
    }

    private val locationCallback = object: LocationCallback() {
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        override fun onLocationResult(locationResult: LocationResult) {
            println("현시각 ${SimpleDateFormat("yy.MM.dd. hh:mm", Locale.KOREA).format(Date(System.currentTimeMillis()))}: >> LocationResult")
            locationResult.locations.forEach {
                println("Latitude: ${it.latitude}, Longitude: ${it.longitude}")
            }
            coroutineScope.launch {
                appDataStore.setCurrentLocationLatitude(locationResult.locations.last().latitude)
                appDataStore.setCurrentLocationLongitude(locationResult.locations.last().longitude)
                println("${SimpleDateFormat("yy.MM.dd. hh:mm:ss", Locale.KOREA).format(Date(System.currentTimeMillis()))} - 새 장소 확인: ${locationResult.locations.last()}")
            }
        }
    }

    fun startTravel() {
        // DB에 History 저장
        val currentTime = System.currentTimeMillis()
        val newTravelHistory = TravelHistory(travelStartTime = currentTime)
        _isLoading.value = true

        val inputData = workDataOf(Pair(TRAVEL_DESTINATION, arrayOf(destination.value.latitude, destination.value.longitude)))

        // Timer Worker 시작
        val workRequest = OneTimeWorkRequestBuilder<TravelWorker>()
            .setInputData(inputData)
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                workManager.enqueue(workRequest)
                appDataStore.setCurrentTravelWorkerId(workRequest.id.toString())
                appDataStore.setDestinationLatLng(candidateDestination)
                createNotification()
                delay(1000L)
                _isLoading.value = false
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cancelTravel() {
        try {
            workManager.cancelAllWork()
            workManager.pruneWork()

            viewModelScope.launch(Dispatchers.IO) {
                appDataStore.setCurrentTravelWorkerId("")
                val lastTravelHistory = appDatabase.getTravelHistoryDao().selectLatestTravelHistory()
                val updatedTravelHistory = lastTravelHistory.copy(travelState = 1)
                val lastTravelLocation = TravelLocation(
                    travelIndex = updatedTravelHistory.travelIndex,
                    arrivalTime = System.currentTimeMillis(),
                    latitude = lastLocationLatitude,
                    longitude = lastLocationLongitude
                )
                appDatabase.getTravelHistoryDao().insertTravelHistoryWithLocation(updatedTravelHistory, lastTravelLocation)
            }
            cancelNotification()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification() {
        val notificationManager = NotificationManagerCompat.from(application)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val channel = NotificationChannel(
                TRAVEL_START_NOTIFICATION_CHANNEL,
                TRAVEL_START_NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    enableLights(true)
                    lightColor = TodayTravelTeal.toArgb()
                    description = "여행중이에요."
                }

            notificationManager.createNotificationChannel(channel)
        }

        val titleNotification = "TodayTravel!"
        val subtitleNotification = "여행중이에요."
        val notification = NotificationCompat
            .Builder(application, TRAVEL_START_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_flag_24)
            .setContentTitle(titleNotification)
            .setContentText(subtitleNotification)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(2, notification.build())
        }
    }

    private fun cancelNotification() {
        val notificationManager = NotificationManagerCompat.from(application)
        notificationManager.cancel(2)
    }

    // 거리 차이(m)를 좌표 차이로 변환
    private fun mapDistanceToCoordinate(distance: Float): Double {
        return (distance / 100000).toDouble()
    }

    fun updateCurrentLatLng(location: LatLng) {
        _currentLocation.value = location
    }

    private val _markedLocations = MutableStateFlow(listOf<TravelLocation>())
    val markedLocations: StateFlow<List<TravelLocation>>
        get() = _markedLocations.asStateFlow()

    fun updateMarkedTravelHistory(travelHistoryWithLocations: TravelHistoryWithLocations?) {
        if (travelHistoryWithLocations == null) {
            _markedLocations.value = emptyList()
            return
        }
        _markedLocations.value = travelHistoryWithLocations.travelLocations
    }

    init {
        viewModelScope.apply {
            launch {
                appDataStore.getTravelRadius().stateIn(viewModelScope).collect {
                    travelRadius = it
                }
            }
            launch {
                appDataStore.getDestinationLatLng().stateIn(viewModelScope).collect {
                    _destination.value = it
                }
            }
            launch {
                appDataStore.getCurrentTravelWorkerId().stateIn(viewModelScope).collect {
                    _isTraveling.value = it != ""
                }
            }
            launch {
                appDataStore.getCurrentLocationLatitude().stateIn(viewModelScope).collect {
                    lastLocationLatitude = it
                }
            }
            launch {
                appDataStore.getCurrentLocationLongitude().stateIn(viewModelScope).collect {
                    lastLocationLongitude = it
                }
            }
            launch(Dispatchers.IO) {
                appDatabase.getTravelHistoryDao().selectLatestTravelHistoryAsFlow().collect {
                    _lastTravelHistoryIndex.value = it?.travelIndex ?: -1
                }
            }
            launch {
                appDataStore.getPreventionOfMapRotation().stateIn(viewModelScope).collect {
                    _preventionOfMapRotation.value = it
                }
            }
        }
    }
}
