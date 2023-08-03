package com.sb.todaytravel.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sb.todaytravel.data.repositories.AppDatabase
import com.sb.todaytravel.data.repositories.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var orderType: OrderType = OrderType.Descend

    private suspend fun getAllTravelHistory(orderType: OrderType) {
        getTravelHistoryJob?.cancel()
        getTravelHistoryJob =
            appDatabase.getTravelHistoryDao().selectAllTravelHistory().stateIn(viewModelScope).onEach { travelHistories ->
                val list = mutableListOf<TravelHistoryWithLocations>()
                delay(200L) // 이 딜레이가 없으면 화면에 표시되지 않음

                travelHistories
                    .sortedWith(
                        comparator = compareBy {
                            if (orderType is OrderType.Ascend) it.travelStartTime else -it.travelStartTime
                        }
                    )
                    .forEach { travelHistory ->
                        val travelLocation = withContext(Dispatchers.IO) {
                            appDatabase.getTravelLocationDao().selectTravelLocations(travelHistory.index)
                        }
                        list.add(TravelHistoryWithLocations(travelHistory.index, travelHistory.travelStartTime, travelLocation))
                    }
                _allTravelHistories.value = list
            }.launchIn(viewModelScope)
    }

    fun switchOrderType() {
        viewModelScope.launch {
            if (orderType is OrderType.Ascend)
                appDataStore.setOrderType(AppDataStore.ORDER_TYPE_DESCEND)
            else
                appDataStore.setOrderType(AppDataStore.ORDER_TYPE_ASCEND)
        }
    }

    init {
        viewModelScope.launch {
            appDataStore.getOrderType().collect { storedOrderType ->
                orderType = if (storedOrderType == 1) OrderType.Descend else OrderType.Ascend
                getAllTravelHistory(orderType)
            }
        }
    }
}