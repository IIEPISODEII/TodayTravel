package com.sb.todaytravel.feature.travel_history

sealed class OrderType {
    object Ascend: OrderType()
    object Descend: OrderType()
}