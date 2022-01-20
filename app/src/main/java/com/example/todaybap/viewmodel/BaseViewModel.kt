package com.example.todaybap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    private val _viewEvent = MutableLiveData<Event<Any>>()
    val viewEvent: LiveData<Event<Any>> = _viewEvent

    fun viewEvent(content: Any) {
        _viewEvent.value = Event(content)
    }
}