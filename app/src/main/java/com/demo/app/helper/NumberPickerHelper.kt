package com.demo.app.helper

import android.view.MotionEvent
import android.view.View
import android.widget.TextView

class NumberPickerHelper(
    private val vMinus: View,
    private val vPlus: View,
    private val vValue: TextView
) {
    private var min: Float = 0f
    private var max: Float = 100f
    private var step: Float = 1f

    private var mValue: Float = 0f

    private var onChanged: ((Float) -> Unit)? = null

    var value: Float
        get() = mValue
        set(value) {
            if (mValue == value) return
            val newValue = value.coerceIn(min, max)
            if (mValue == newValue) return
            mValue = newValue
            updateViews()

        }

    fun onChanged(action:((Float) -> Unit)? = null) {
        onChanged = action
    }


    init {
        vMinus.setOnTouchListener(this::onTouch)
        vPlus.setOnTouchListener(this::onTouch)
        vMinus.setOnClickListener(this::onClick)
        vPlus.setOnClickListener(this::onClick)
        updateViews()
    }

    fun init(min: Float = 0f, max: Float = 100f, step: Float = 1f) {
        if (min > max) {
            throw IllegalArgumentException("min > max")
        }

        this.min = min
        this.max = max
        this.step = step
        this.mValue = mValue.coerceIn(min, max)
        updateViews()
    }

    private fun updateViews() {
        vValue.text = "$mValue"
        vMinus.isEnabled = mValue != min
        vPlus.isEnabled = mValue != max
    }


    private var downAt: Long = 0
    private var lastAt: Long = 0
    private fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downAt = System.currentTimeMillis()
                lastAt = downAt
            }
            MotionEvent.ACTION_MOVE -> {
                if (lastAt > 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastAt > 500) {
                        onClick(view)
                        lastAt = now
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (lastAt - downAt > 500) {
                    onChanged?.invoke(mValue)
                }
                downAt = 0
                lastAt = 0
            }
        }
        return false
    }

    private fun onClick(view: View) {
        val sign = if (view == vMinus) -1f else 1f
        mValue = (mValue + sign * step).coerceIn(min, max)
        updateViews()
        if (lastAt - downAt <= 500) {
            onChanged?.invoke(mValue)
        }
    }
}
