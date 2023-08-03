package com.sb.todaytravel.data.datasources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

const val APP_SETTINGS: String = "app_settings"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(APP_SETTINGS)