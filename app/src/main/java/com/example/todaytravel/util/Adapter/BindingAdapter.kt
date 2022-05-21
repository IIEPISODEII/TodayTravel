package com.example.todaytravel.util.Adapter

import android.view.View
import androidx.databinding.BindingAdapter

object BindingAdapter {
    @JvmStatic
    @BindingAdapter("isVisible")
    fun setVisibile(view: View, isRunning: Boolean) {
        view.visibility = if (isRunning) View.VISIBLE else View.GONE
    }
}