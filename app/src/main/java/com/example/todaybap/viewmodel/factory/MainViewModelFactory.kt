package com.example.todaybap.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todaybap.repo.NaverMapRepository
import com.example.todaybap.repo.SharedPreferenceRepository

class MainViewModelFactory(
    private val naverMapRepository: NaverMapRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass
        .getConstructor(NaverMapRepository::class.java)
        .newInstance(naverMapRepository)
}