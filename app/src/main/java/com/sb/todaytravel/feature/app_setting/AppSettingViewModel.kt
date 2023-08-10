package com.sb.todaytravel.feature.app_setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val appDatabase: AppDatabase
): ViewModel() {

    private val _travelRadius = MutableStateFlow(0)
    val travelRadius: StateFlow<Int>
        get() = _travelRadius.asStateFlow()

    fun setTravelRadius(radius: Int) {
        viewModelScope.launch {
            appDataStore.setTravelRadius(radius)
        }
    }

    private suspend fun getTravelRadius() {
        appDataStore.getTravelRadius().stateIn(viewModelScope).collect {
            _travelRadius.emit(it)
        }
    }

    fun deleteHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.getTravelHistoryDao().deleteAllTravelHistories()
            appDatabase.getTravelLocationDao().deleteAllTravelLocations()
        }
    }


    private val _preventionOfMapRotation = MutableStateFlow(true)
    val preventionOfMapRotation: StateFlow<Boolean>
        get() = _preventionOfMapRotation.asStateFlow()
    
    fun setPreventionOfMapRotation(prevention: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.setPreventionOfMapRotation(prevention)
        }
    }

    init {
        viewModelScope.launch {
            getTravelRadius()
        }
        viewModelScope.launch {
            appDataStore.getPreventionOfMapRotation().stateIn(viewModelScope).collect {
                _preventionOfMapRotation.emit(it)
            }
        }
    }
}