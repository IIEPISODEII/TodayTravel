package com.example.todaybap.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import androidx.lifecycle.ViewModelProvider
import com.example.todaybap.MainActivity
import com.example.todaybap.R
import com.example.todaybap.viewmodel.MainViewModel

class TimerDialog(context: Context) : Dialog(context) {
    private val mDialogView by lazy { layoutInflater.inflate(R.layout.dialog_custom_timer, null) }
    private lateinit var viewModel: MainViewModel
    private lateinit var mTvHour: NumberPicker
    private lateinit var mTvMinute: NumberPicker
    private lateinit var mButtonSave: Button
    private lateinit var mButtonExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("다이얼로그 생성")
        setContentView(mDialogView)
        viewModel = ViewModelProvider(this.ownerActivity as MainActivity)[MainViewModel::class.java]
        mTvHour = mDialogView.findViewById(R.id.custom_timer_button_hour)
        mTvMinute = mDialogView.findViewById(R.id.custom_timer_button_minute)
        mButtonSave = mDialogView.findViewById(R.id.custom_timer_button_save)
        mButtonExit = mDialogView.findViewById(R.id.custom_timer_button_exit)

        mTvHour.run {
            if (viewModel != null) value = viewModel.getTravelTime() / 60
            minValue = 0
            maxValue = 24
        }
        mTvMinute.run {
            value = viewModel.getTravelTime() - viewModel.getTravelTime() / 60
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


}