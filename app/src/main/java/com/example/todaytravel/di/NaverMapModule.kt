package com.example.todaytravel.di

import com.example.todaytravel.data.navermapapi.retrofit.NaverMapService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class NaverMapModule {

    @Singleton
    @Provides
    fun provideNaverMapService() : NaverMapService {
        return NaverMapService.getRetrofitInstance()
    }
}