package com.example.todaybap.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.example.todaybap.repo.NaverMapRepository
import com.example.todaybap.repo.SharedPreferenceRepository
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.*
import util.Extension.toDMS
import kotlin.math.sqrt

class MainViewModel : BaseViewModel() {

    companion object {
        const val EVENT_UPDATE_CURRENT_LOCATION = 1000
    }
    private val M_PER_MINUTE: Float = 66.67F
    private val mSharedPreferenceRepository = SharedPreferenceRepository.getInstance()
    private val mNaverMapRepository = NaverMapRepository.getInstance()

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
    fun getCoordInfo(query: LatLng) {
        viewModelScope.launch {
            val apiResponse = mNaverMapRepository.getCoordinateInfo(query = query)
            val apiResponseBody = apiResponse.body()
            val apiResponseCode = apiResponse.code()

            // 원래는 try catch 써서 404 같은 에러 처리해야 함
            var numberAddress = "결과 없음"
            var roadnameAddress = "결과 없음"

            if (apiResponseBody?.status?.code == 0) {
                val addressBase = apiResponseBody.results[0]?.region
                // 도,시/구/동/리
                numberAddress = "${addressBase?.area1?.name} "
                    .plus("${addressBase?.area2?.name} ")
                    .plus("${addressBase?.area3?.name} ")
                    .plus("${addressBase?.area4?.name} ")
                if (apiResponseBody.results.lastIndex >= 1) {
                    // 번지수
                    numberAddress = numberAddress
                        .plus("${apiResponseBody.results[1]?.land?.number1}")
                    val apiResponseBodyLandNumber2 = apiResponseBody.results[1]?.land?.number2
                    // 번지수-번지수 ex) 102-3번지
                    if (apiResponseBodyLandNumber2 != null && apiResponseBodyLandNumber2.isNotBlank()) {
                        numberAddress = numberAddress.plus("-${apiResponseBody.results[1]?.land?.number2}")
                    }
                    // 길 + 건물번호
                    if (apiResponseBody.results.lastIndex >= 3) {
                        val roadAddrBase = apiResponseBody.results[3]?.region
                        roadnameAddress = "${roadAddrBase?.area1?.name} "
                            .plus("${roadAddrBase?.area2?.name} ")
                            .plus("${roadAddrBase?.area3?.name} ")
                        val apiResponseRoadAddrArea4Name = roadAddrBase?.area4?.name
                        if (apiResponseRoadAddrArea4Name != null && apiResponseRoadAddrArea4Name.isNotBlank()) roadnameAddress = roadnameAddress.plus(roadAddrBase.area4.name)
                        roadnameAddress = roadnameAddress
                            .plus(apiResponseBody.results[3]?.land?.name)
                            .plus(" ${apiResponseBody.results[3]?.land?.number1}")
                    }
                }
            }
            _currentCoordInfo.value = CoordInfoData(query.latitude.toDMS(), query.longitude.toDMS(), numberAddress, roadnameAddress)
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
            val walkingTime = mSharedPreferenceRepository.getWalkingTime()
            val distance = calculateRaidus(walkingTime)
            val distanceX: Float = distance * Math.random().toFloat()
            val randomDistanceX: Float = if (Math.random() >= 0.5) distanceX else -distanceX
            val distanceY: Float = sqrt(distance * distance - distanceX * distanceX)
            val randomDistanceY: Float = if (Math.random() >= 0.5 ) distanceY else -distanceY

            val coorX = mapDistanceToCoordinate(randomDistanceX)
            val coorY = mapDistanceToCoordinate(randomDistanceY)

            if (mLatitude != null) newLatitude = mLatitude + coorY
            if (mLongitude != null) newLongitude = mLongitude + coorX
        }
        println("새 위도: $newLatitude, 새 경도: $newLongitude")
        _newLatLng.value = LatLng(newLatitude, newLongitude)
    }

    // 주어진 시간을 성인 평균 보폭 거리(m)로 변환
    private fun calculateRaidus(time: Int): Float {
        return (M_PER_MINUTE * time)
    }

    // 거리 차이(m)를 좌표 차이로 변환
    private fun mapDistanceToCoordinate(distance: Float): Double {
        return (distance/100000).toDouble()
    }

    fun updateCurrentLocation(lastLocation: Location?) {
        mCurrentLocation.value = lastLocation
    }

    fun setTravelHour(h: Int) {
        mSharedPreferenceRepository.setWalkingHour(h)
    }

    fun setTravelMinute(m: Int) {
        mSharedPreferenceRepository.setWalkingMinute(m)
    }

    fun getTravelTime(): Int {
        return mSharedPreferenceRepository.getWalkingTime()
    }

    private fun updateCurrentLocation() = viewEvent(EVENT_UPDATE_CURRENT_LOCATION)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}

data class CoordInfoData(val lati: String, val longi: String, val address: String, val roadAddr: String)
