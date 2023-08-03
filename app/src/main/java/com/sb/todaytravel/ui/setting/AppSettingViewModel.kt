package com.sb.todaytravel.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.todaytravel.data.repositories.AppDataStore
import com.sb.todaytravel.data.repositories.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingViewModel @Inject constructor(
    private val appSettingRepo: AppDataStore,
    private val appDatabase: AppDatabase
): ViewModel() {

    private val _travelRadius = MutableStateFlow(0)
    val travelRadius: StateFlow<Int>
        get() = _travelRadius

    fun setTravelRadius(radius: Int) {
        viewModelScope.launch {
            appSettingRepo.setTravelRadius(radius)
        }
    }

    private suspend fun getTravelRadius() {
        appSettingRepo.getTravelRadius().stateIn(viewModelScope).collect {
            _travelRadius.emit(it)
        }
    }

    fun deleteHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.getTravelHistoryDao().deleteAllTravelHistories()
            appDatabase.getTravelLocationDao().deleteAllTravelLocations()
        }
    }

    init {
        viewModelScope.launch {
            getTravelRadius()
        }
    }
}