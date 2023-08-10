package com.sb.todaytravel.feature.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.naver.maps.geometry.LatLng
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation
import com.sb.todaytravel.feature.travel_manage.TravelWorker
import com.sb.todaytravel.feature.travel_history.TravelHistoryWithLocations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val appDatabase: AppDatabase,
    private val application: Application
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application.applicationContext)
    private var currentTravelWorkerId = ""

    private val _currentLocation = MutableStateFlow((LatLng(127.3, 21.3)))
    val currentLocation: StateFlow<LatLng>
        get() = _currentLocation.asStateFlow()

    var destinationLatLng = LatLng(127.3, 21.3)
        private set

    private var travelRadius = 0

    private val _isTraveling = MutableStateFlow(false)
    val isTraveling: StateFlow<Boolean>
        get() = _isTraveling.asStateFlow()

    private val _lastTravelHistoryIndex = MutableStateFlow(-1)
    val lastTravelHistoryIndex: StateFlow<Int>
        get() = _lastTravelHistoryIndex.asStateFlow()

    private var lastLocationLatitude = 0F
    private var lastLocationLongitude = 0F

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

        destinationLatLng = LatLng(newLatitude, newLongitude)
    }

    fun setDestination(latLng: LatLng) {
        destinationLatLng = latLng
    }

    fun startTravel() {
        // DB에 History 저장
        val currentTime = System.currentTimeMillis()
        val newTravelHistory = TravelHistory(travelStartTime = currentTime)

        // Timer Worker 시작
        val workRequest = OneTimeWorkRequestBuilder<TravelWorker>()
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                appDatabase.getTravelHistoryDao().insertTravelHistory(newTravelHistory)
                appDataStore.setCurrentTravelWorkerId(workRequest.id.toString())
                workManager.enqueue(workRequest)
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
                val lastTravelIndex = appDatabase.getTravelHistoryDao().selectLatestTravelHistory().index
                val lastTravelLocation = TravelLocation(
                    index = lastTravelIndex,
                    arrivalTime = System.currentTimeMillis(),
                    latitude = lastLocationLatitude,
                    longitude = lastLocationLongitude
                )
                appDatabase.getTravelLocationDao().insertTravelLocation(lastTravelLocation)
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    // 거리 차이(m)를 좌표 차이로 변환
    private fun mapDistanceToCoordinate(distance: Float): Double {
        return (distance / 100000).toDouble()
    }

    fun updateCurrentLatLng(location: LatLng) {
        _currentLocation.value = location
    }

    private val _markedLocations = MutableStateFlow(mutableListOf<TravelLocation>())
    val markedLocations = _markedLocations.asStateFlow()

    fun updateMarkedTravelHistory(travelHistoryWithLocations: TravelHistoryWithLocations) {
        _markedLocations.value.clear()
        _markedLocations.value.addAll(travelHistoryWithLocations.travelLocations)
    }

    init {
        viewModelScope.apply {
            launch {
                appDataStore.getTravelRadius().stateIn(viewModelScope).collect {
                    travelRadius = it
                }
            }
            launch {
                appDataStore.getCurrentTravelWorkerId().stateIn(viewModelScope).collect {
                    _isTraveling.value = it != ""
                    currentTravelWorkerId = it
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
                    _lastTravelHistoryIndex.value = it?.index ?: -1
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
