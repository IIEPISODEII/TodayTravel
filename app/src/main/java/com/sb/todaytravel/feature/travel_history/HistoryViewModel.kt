package com.sb.todaytravel.feature.travel_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val appDatabase: AppDatabase
): ViewModel() {

    private val _allTravelHistories = MutableStateFlow<List<TravelHistoryWithLocations>>(mutableListOf())
    val allTravelHistories: StateFlow<List<TravelHistoryWithLocations>>
        get() = _allTravelHistories

    private var getTravelHistoryJob: Job? = null

    private val _orderType = MutableStateFlow<OrderType>(OrderType.Descend)
    val orderType: StateFlow<OrderType>
        get() = _orderType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private suspend fun getAllTravelHistory(orderType: OrderType) {
        appDatabase.getTravelLocationDao().selectAllTravelLocationsGrouped().stateIn(viewModelScope).map { records ->
            if (orderType == OrderType.Descend) records.map { it.toTravelHistoryWithLocations() }
            else records.sortedWith(comparator = compareBy { it.history.travelIndex }).map { it.toTravelHistoryWithLocations() }
        }.onEach {
            _isLoading.value = true
            _allTravelHistories.value = it
            _isLoading.value = false
        }.launchIn(viewModelScope)
    }

    fun deleteTravelHistory(travelHistoryIndex: Long) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.getTravelHistoryDao().deleteTravelHistory(travelHistoryIndex)
            _isLoading.value = true
        }
    }

    fun switchOrderType() {
        viewModelScope.launch {
            if (_orderType.value is OrderType.Ascend)
                appDataStore.setOrderType(AppDataStore.ORDER_TYPE_DESCEND)
            else
                appDataStore.setOrderType(AppDataStore.ORDER_TYPE_ASCEND)
        }
    }

    init {
        viewModelScope.launch {
            appDataStore.getOrderType().collect { storedOrderType ->
                _orderType.value = if (storedOrderType == 1) OrderType.Descend else OrderType.Ascend
                getAllTravelHistory(_orderType.value)
            }
        }
    }
}