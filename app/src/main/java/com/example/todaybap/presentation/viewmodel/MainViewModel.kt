package com.example.todaybap.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.example.todaybap.domain.usecase.GetAddressFromNaverUsecase
import com.example.todaybap.domain.usecase.GetWalkingTimeUsecase
import com.example.todaybap.domain.usecase.SetWalkingHourUsecase
import com.example.todaybap.domain.usecase.SetWalkingMinuteUsecase
import com.example.todaybap.util.Extension.toDMS
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAddressFromNaverUsecase: GetAddressFromNaverUsecase,
    private val getWalkingTimeUsecase: GetWalkingTimeUsecase,
    private val setWalkingHourUsecase: SetWalkingHourUsecase,
    private val setWalkingMinuteUsecase : SetWalkingMinuteUsecase
    ) : BaseViewModel() {

    companion object {
        const val EVENT_UPDATE_CURRENT_LOCATION = 1000
    }

    private val M_PER_MINUTE: Float = 66.67F

    private var _mLatLng: MutableLiveData<LatLng> = MutableLiveData()
    val mLatLng: LiveData<LatLng> = _mLatLng
    private var _newLatLng: MutableLiveData<LatLng> = MutableLiveData()
    val newLatLng: LiveData<LatLng> = _newLatLng

    fun setMyLatLng(new: LatLng) {
        _mLatLng.value = new
    }

    private var _mCurrentLocation: MutableLiveData<Location> = MutableLiveData()
    val mCurrentLocation = _mCurrentLocation

    private var _currentCoordInfo: MutableLiveData<CoordInfoData> = MutableLiveData()
    val currentCoordInfo: LiveData<CoordInfoData>
        get() = _currentCoordInfo

    // 주소 정보 불러오기
    fun updateCoordInfo(query: LatLng) {
        viewModelScope.launch {
            val apiResponse = getAddressFromNaverUsecase.getAddressFromNaver(query = query)
            val apiResponseBody = apiResponse.body()

            // 원래는 try catch 써서 404 같은 에러 처리해야 함
            var numberAddress = StringBuilder("결과 없음")
            var roadnameAddress = StringBuilder("결과 없음")

            if (apiResponseBody?.status?.code == 0) {
                val addressBase = apiResponseBody.results[0]?.region
                // 도,시/구/동/리
                numberAddress = StringBuilder("${addressBase?.area1?.name} ")
                    .append("${addressBase?.area2?.name} ")
                    .append("${addressBase?.area3?.name} ")
                    .append("${addressBase?.area4?.name} ")
                if (apiResponseBody.results.lastIndex >= 1) {
                    // 번지수
                    numberAddress = numberAddress
                        .append("${apiResponseBody.results[1]?.land?.number1}")
                    val apiResponseBodyLandNumber2 = apiResponseBody.results[1]?.land?.number2
                    // 번지수-번지수 ex) 102-3번지
                    if (apiResponseBodyLandNumber2 != null && apiResponseBodyLandNumber2.isNotBlank()) {
                        numberAddress =
                            numberAddress.append("-${apiResponseBody.results[1]?.land?.number2}")
                    }
                    // 길 + 건물번호
                    if (apiResponseBody.results.lastIndex >= 3) {
                        val roadAddrBase = apiResponseBody.results[3]?.region
                        roadnameAddress = StringBuilder("${roadAddrBase?.area1?.name} ")
                            .append("${roadAddrBase?.area2?.name} ")
                            .append("${roadAddrBase?.area3?.name} ")
                        val apiResponseRoadAddrArea4Name = roadAddrBase?.area4?.name
                        if (apiResponseRoadAddrArea4Name != null && apiResponseRoadAddrArea4Name.isNotBlank()) roadnameAddress =
                            roadnameAddress.append(roadAddrBase.area4.name)
                        roadnameAddress = roadnameAddress
                            .append(apiResponseBody.results[3]?.land?.name)
                            .append(" ${apiResponseBody.results[3]?.land?.number1}")
                    }
                }
            }
            _currentCoordInfo.value = CoordInfoData(
                query.latitude.toDMS(),
                query.longitude.toDMS(),
                numberAddress.toString(),
                roadnameAddress.toString()
            )
        }
    }

    // 장소 찾기
    fun findLocationFromCurrentLocation() {
        val mLatitude = mCurrentLocation.value?.latitude
        val mLongitude = mCurrentLocation.value?.longitude

        if (mLatitude == null || mLongitude == null) {
            updateCurrentLocation()
        }
        println("기존 위도: $mLatitude, 기존 경도: $mLongitude")

        var newLatitude = 0.0
        var newLongitude = 0.0
        viewModelScope.launch {
            val walkingTime = getWalkingTimeUsecase.getWalkingTime()
            val distance = calculateRaidus(walkingTime)
            val distanceX: Float = distance * Math.random().toFloat()
            val randomDistanceX: Float = if (Math.random() >= 0.5) distanceX else -distanceX
            val distanceY: Float = sqrt(distance * distance - distanceX * distanceX)
            val randomDistanceY: Float = if (Math.random() >= 0.5) distanceY else -distanceY

            val coorX = mapDistanceToCoordinate(randomDistanceX)
            val coorY = mapDistanceToCoordinate(randomDistanceY)

            if (mLatitude != null) newLatitude = mLatitude + coorY
            if (mLongitude != null) newLongitude = mLongitude + coorX
        }
        _newLatLng.value = LatLng(newLatitude, newLongitude)
    }

    // 주어진 시간을 성인 평균 보폭 거리(m)로 변환
    private fun calculateRaidus(time: Int): Float {
        return (M_PER_MINUTE * time)
    }

    // 거리 차이(m)를 좌표 차이로 변환
    private fun mapDistanceToCoordinate(distance: Float): Double {
        return (distance / 100000).toDouble()
    }

    fun updateCurrentLocation(lastLocation: Location?) {
        mCurrentLocation.value = lastLocation
    }

    fun setTravelHour(h: Int) {
        setWalkingHourUsecase.setTravelHour(h)
    }

    fun setTravelMinute(m: Int) {
        setWalkingMinuteUsecase.setWalkingMinute(m)
    }

    fun getTravelTime(): Int {
        return getWalkingTimeUsecase.getWalkingTime()
    }

    private fun updateCurrentLocation() = viewEvent(EVENT_UPDATE_CURRENT_LOCATION)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    data class CoordInfoData(
        val lati: String,
        val longi: String,
        val address: String,
        val roadAddr: String
    )
}
