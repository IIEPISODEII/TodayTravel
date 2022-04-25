package com.example.todaytravel.presentation.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.lifecycle.ViewModelProvider
import com.example.todaytravel.MainActivity
import com.example.todaytravel.R
import com.example.todaytravel.presentation.viewmodel.MainViewModel

class TimerDialog(context: Context) : Dialog(context) {
    private val mDialogView by lazy { layoutInflater.inflate(R.layout.dialog_custom_timer, null) }
    private lateinit var viewModel: MainViewModel
    private lateinit var mTvHour: NumberPicker
    private lateinit var mTvMinute: NumberPicker
    private lateinit var mButtonSave: Button
    private lateinit var mButtonExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(mDialogView)
        viewModel = ViewModelProvider(this.ownerActivity as MainActivity)[MainViewModel::class.java]
        mTvHour = mDialogView.findViewById(R.id.custom_timer_button_hour)
        mTvMinute = mDialogView.findViewById(R.id.custom_timer_button_minute)
        mButtonSave = mDialogView.findViewById(R.id.custom_timer_button_save)
        mButtonExit = mDialogView.findViewById(R.id.custom_timer_button_exit)

        mTvHour.run {
            minValue = 0
            maxValue = 24
        }
        mTvMinute.run {
            minValue = 0
            maxValue = 59
        }
        mButtonSave.setOnClickListener {
            viewModel.run {
                setTravelHour(mTvHour.value)
                setTravelMinute(mTvMinute.value)
            }
            dismiss()
        }
        mButtonExit.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val initialTravelTime = viewModel.getTravelTime()
        mTvHour.value = initialTravelTime/60
        mTvMinute.value = initialTravelTime - initialTravelTime / 60
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}