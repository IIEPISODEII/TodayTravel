package com.example.todaybap.di

import android.content.Context
import android.content.SharedPreferences
import com.example.todaybap.domain.repo.SharedPreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class SharedPreferenceModule {

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferenceRepository {
        return SharedPreferenceRepository.getInstance(context)
    }
}