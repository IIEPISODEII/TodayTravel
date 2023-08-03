package com.sb.todaytravel.ui.history

sealed class OrderType {
    object Ascend: OrderType()
    object Descend: OrderType()
}