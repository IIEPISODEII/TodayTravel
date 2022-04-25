package com.example.todaytravel.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todaytravel.data.repo.NaverMapRepository

class MainViewModelFactory(
    private val naverMapRepository: NaverMapRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass
        .getConstructor(NaverMapRepository::class.java)
        .newInstance(naverMapRepository)
}