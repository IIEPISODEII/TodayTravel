package com.example.todaybap.di

import com.example.todaybap.data.naverapi.retrofit.NaverMapService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class NaverModule {

    @Singleton
    @Provides
    fun provideNaverMapService() : NaverMapService {
        return NaverMapService.getRetrofitInstance()
    }
}