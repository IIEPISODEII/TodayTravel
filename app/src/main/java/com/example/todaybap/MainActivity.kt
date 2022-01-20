package com.example.todaybap

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.todaybap.databinding.ActivityMainBinding
import com.example.todaybap.repo.NaverMapRepository
import com.example.todaybap.viewmodel.MainViewModel
import com.example.todaybap.viewmodel.factory.MainViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.internal.NaverMapAccessor
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var vm: MainViewModel
    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mBinding: ActivityMainBinding
    private val INIT_MAP_POSITION = LatLng(37.5670135, 126.9783740)
    private val newLocationMarker = Marker()

    // 여행시간 설정 다이얼로그
    private lateinit var mTimerDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.run {
            viewModel = this@MainActivity.vm
            lifecycleOwner = this@MainActivity
        }
        // 네이버맵 호출
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
        vm.run {
            currentCoordInfo.observe(this@MainActivity, {})
            getCoordInfo(INIT_MAP_POSITION)
            mCurrentLocation.observe(this@MainActivity, {})
            newLatLng.observe(this@MainActivity, {
                newLocationMarker.map = null
                newLocationMarker.position = it
                newLocationMarker.map = naverMap
                naverMap.moveCamera(
                    CameraUpdate.scrollTo(it)
                        .reason(CameraUpdate.REASON_LOCATION)
                        .animate(CameraAnimation.Easing, 2000)
                )
            })
            viewEvent.observe(this@MainActivity, {
                it.getContentIfNotHandled()?.let { event ->
                    when (event) {
                        MainViewModel.EVENT_UPDATE_CURRENT_LOCATION -> {
                            runBlocking {
                                println("위치 소스: ${naverMap.locationSource}")
                            }
                        }
                    }
                }
            })
        }
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.maxZoom = 21.toDouble()
        naverMap.minZoom = 0.toDouble()

        // 카메라를 cameraUpdate 위치로 옮김
        cameraUpdate = CameraUpdate.scrollTo(INIT_MAP_POSITION)
            .reason(3)
            .animate(CameraAnimation.Easing, 2000)
            .finishCallback {
                Toast.makeText(this, "완료", Toast.LENGTH_SHORT).show()
            }
            .cancelCallback {
                Toast.makeText(this, "취소", Toast.LENGTH_SHORT).show()
            }
        naverMap.moveCamera(cameraUpdate)

        // 위치 추적 기능 활성화
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        locationSource.activate {
            vm.updateCurrentLocation(locationSource.lastLocation)
        }
        naverMap.locationSource = this.locationSource

        this.naverMap.uiSettings.isCompassEnabled = true
        this.naverMap.uiSettings.isScaleBarEnabled = true
        this.naverMap.uiSettings.isLocationButtonEnabled = true

        newLocationMarker.run {
            position = INIT_MAP_POSITION
            map = naverMap
        }
        naverMap.setOnMapClickListener { pointF, latLng ->
            newLocationMarker.map = null
            newLocationMarker.position = latLng
            newLocationMarker.map = naverMap
            vm.setMyLatLng(new = latLng)
            vm.getCoordInfo(latLng)
            naverMap.moveCamera(
                CameraUpdate.scrollTo(latLng)
                    .reason(CameraUpdate.REASON_LOCATION)
                    .animate(CameraAnimation.Easing, 2000)
            )
        }
        naverMap.addOnLocationChangeListener {
            vm.updateCurrentLocation(it)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationSource.deactivate()
    }
}