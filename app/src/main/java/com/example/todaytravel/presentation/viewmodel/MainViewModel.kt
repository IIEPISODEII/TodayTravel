package com.example.todaytravel.presentation.viewmodel

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.todaytravel.domain.usecase.GetAddressFromNaverUsecase
import com.example.todaytravel.domain.usecase.GetWalkingTimeUsecase
import com.example.todaytravel.domain.usecase.SetWalkingHourUsecase
import com.example.todaytravel.domain.usecase.SetWalkingMinuteUsecase
import com.example.todaytravel.util.Extension.toDMS
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAddressFromNaverUsecase: GetAddressFromNaverUsecase,
    private val getWalkingTimeUsecase: GetWalkingTimeUsecase,
    private val setWalkingHourUsecase: SetWalkingHourUsecase,
    private val setWalkingMinuteUsecase: SetWalkingMinuteUsecase
) : BaseViewModel() {

    companion object {
        const val EVENT_UPDATE_CURRENT_LOCATION = 1000
    }

    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> = _isRunning
    fun setRunning(b: Boolean) {
        _isRunning.value = b
    }

    private val M_PER_MINUTE: Float = 66.67F

    private var _mLatLng: MutableLiveData<LatLng> = MutableLiveData()
    private var _newRandomLatLng: MutableLiveData<LatLng> = MutableLiveData()
    val newRandomLatLng: LiveData<LatLng> = _newRandomLatLng

    fun setMyLatLng(new: LatLng) {
        _mLatLng.value = new
    }

    private var _mCurrentLocation: MutableLiveData<Location> = MutableLiveData()
    val mCurrentLocation = _mCurrentLocation

    private var _markedCoordInfo: MutableLiveData<CoordInfoData> = MutableLiveData()
    val markedCoordInfo: LiveData<CoordInfoData>
        get() = _markedCoordInfo


    // ?????? ?????? ????????????
    fun updateCoordInfo(query: LatLng) {
        viewModelScope.launch {
            val apiResponse = getAddressFromNaverUsecase.getAddressFromNaver(query = query)
            val apiResponseBody = apiResponse.body()

            // ????????? try catch ?????? 404 ?????? ?????? ???????????? ???
            var numberAddress = StringBuilder("?????? ??????")
            var roadnameAddress = StringBuilder("?????? ??????")

            if (apiResponseBody?.status?.code == 0) {
                val addressBase = apiResponseBody.results[0]?.region
                // ???,???/???/???/???
                numberAddress = StringBuilder("${addressBase?.area1?.name} ")
                    .append("${addressBase?.area2?.name} ")
                    .append("${addressBase?.area3?.name} ")
                    .append("${addressBase?.area4?.name} ")
                if (apiResponseBody.results.lastIndex >= 1) {
                    // ?????????
                    numberAddress = numberAddress
                        .append("${apiResponseBody.results[1]?.land?.number1}")
                    val apiResponseBodyLandNumber2 = apiResponseBody.results[1]?.land?.number2
                    // ?????????-????????? ex) 102-3??????
                    if (apiResponseBodyLandNumber2 != null && apiResponseBodyLandNumber2.isNotBlank()) {
                        numberAddress =
                            numberAddress.append("-${apiResponseBody.results[1]?.land?.number2}")
                    }
                    // ??? + ????????????
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
            _markedCoordInfo.value = CoordInfoData(
                query.latitude.toDMS(),
                query.longitude.toDMS(),
                numberAddress.toString(),
                roadnameAddress.toString()
            )
        }
    }

    // ?????? ??????
    fun findRandomLocationAroundCurrentLocation() {
        val mLatitude = mCurrentLocation.value?.latitude
        val mLongitude = mCurrentLocation.value?.longitude
        if (mLatitude == null || mLongitude == null) {
            updateCurrentLocation()
        }

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
        _newRandomLatLng.value = LatLng(newLatitude, newLongitude)
    }

    // ????????? ????????? ?????? ?????? ?????? ??????(m)??? ??????
    private fun calculateRaidus(time: Int): Float {
        return (M_PER_MINUTE * time)
    }

    // ?????? ??????(m)??? ?????? ????????? ??????
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

    fun setTimer(ctx: Context, workRequest: OneTimeWorkRequest) {
        WorkManager.getInstance(ctx).apply {
            cancelAllWork()
            enqueue(workRequest)
            setRunning(true)
        }
    }

    fun checkDestination(ctx: Context) {
        var arrival = false
        updateCurrentLocation()

        if (mCurrentLocation.value != null) arrival =
            (mCurrentLocation.value!!.latitude - _newRandomLatLng.value!!.longitude).pow(2.0) + (mCurrentLocation.value!!.longitude - _newRandomLatLng.value!!.latitude) < 0.1.pow(6)

        if (arrival) {
            cancelTimer(ctx)
            Toast.makeText(ctx, "??????!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(ctx, "?????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelTimer(ctx: Context) {
        WorkManager.getInstance(ctx).cancelAllWork()
    }

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
