package com.example.todaytravel

import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.todaytravel.databinding.ActivityMainBinding
import com.example.todaytravel.presentation.view.SocialShareDialog
import com.example.todaytravel.presentation.view.TimerDialog
import com.example.todaytravel.presentation.viewmodel.MainViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var vm: MainViewModel
    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mBinding: ActivityMainBinding
    private val INIT_MAP_POSITION = LatLng(37.5670135, 126.9783740)
    private val newLocationMarker = Marker()

    // 여행시간 설정 다이얼로그
    private val mTimerDialog by lazy { TimerDialog(this) }
    private val mSharedDialog by lazy { SocialShareDialog(this) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this)[MainViewModel::class.java]

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        with(mBinding) {
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
            currentCoordInfo.observe(this@MainActivity) {}
            updateCoordInfo(INIT_MAP_POSITION)
            mCurrentLocation.observe(this@MainActivity) {}
            newLatLng.observe(this@MainActivity) {
                newLocationMarker.map = null
                newLocationMarker.position = it
                newLocationMarker.map = naverMap
                naverMap.moveCamera(
                    CameraUpdate.scrollTo(it)
                        .reason(CameraUpdate.REASON_LOCATION)
                        .animate(CameraAnimation.Easing, 2000)
                )
            }
            viewEvent.observe(this@MainActivity) {
                it.getContentIfNotHandled()?.let { event ->
                    when (event) {
                        MainViewModel.EVENT_UPDATE_CURRENT_LOCATION -> {
                            naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                            locationSource = FusedLocationSource(
                                this@MainActivity,
                                LOCATION_PERMISSION_REQUEST_CODE
                            )
                            updateCurrentLocation(locationSource.lastLocation)
                        }
                    }
                }
            }
        }

        // 타이머 설정 다이얼로그 생성
        mBinding.mainActivityTimerSetting.apply {
            setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        (v as ImageButton).setImageResource(R.drawable.ic_timer_avd)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        mTimerDialog.setOwnerActivity(this@MainActivity)
                        mTimerDialog.show()
                        (v as ImageButton).setImageResource(R.drawable.ic_timer_avd_reverse)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    else -> {
                    }
                }
                true
            }
        }
        mBinding.mainActivitySearchLocation.apply {
            setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        (v as ImageButton).setImageResource(R.drawable.ic_search_avd)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        vm.findRandomLocationAroundCurrentLocation()
                        (v as ImageButton).setImageResource(R.drawable.ic_search_avd_reverse)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    else -> {
                    }
                }
                true
            }
        }
        mBinding.mainActivityShareButton.apply {
            setOnTouchListener { v, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        (v as ImageButton).setImageResource(R.drawable.ic_share_avd)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        mSharedDialog.setOwnerActivity(this@MainActivity)
                        mSharedDialog.show()
                        (v as ImageButton).setImageResource(R.drawable.ic_share_avd_reverse)
                        (v.drawable as AnimatedVectorDrawable).start()
                    }
                    else -> {
                    }
                }
                true
            }
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
            vm.updateCoordInfo(latLng)
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
                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationSource.deactivate()
    }
}