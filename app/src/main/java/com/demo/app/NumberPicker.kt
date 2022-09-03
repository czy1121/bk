package com.demo.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.demo.app.databinding.ViewNumberPickerBinding
import com.demo.app.helper.NumberPickerHelper
import me.reezy.cosmo.bk.BKDrawable

class NumberPicker(context: Context, attrs: AttributeSet? = null) : LinearLayoutCompat(context, attrs) {

    private val binding = ViewNumberPickerBinding.inflate(LayoutInflater.from(context), this)

    val helper by lazy {
        NumberPickerHelper(binding.minus, binding.plus, binding.value)
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker)
        val valueFrom = a.getFloat(R.styleable.NumberPicker_android_valueFrom, 0f)
        val valueTo = a.getFloat(R.styleable.NumberPicker_android_valueTo, 1f)
        val valueStep = a.getFloat(R.styleable.NumberPicker_android_stepSize, 1f)
        a.recycle()

        helper.init(valueFrom, valueTo, valueStep)
    }
}