package com.demo.app.helper

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed

class OptionPickerHelper<T>(private val container: ViewGroup, private val values: Array<T>) {

    private var onChanged: ((T) -> Unit)? = null

    private var mValue: T? = null

    var value: T?
        get() = mValue
        set(value) {
            if (mValue == value) return
            mValue = value
            values.forEachIndexed { index, it ->
                if (value == it) {
                    select(container.getChildAt(index), false)
                    return
                }
            }
        }


    init {
        container.forEach {
            it.setOnClickListener { view ->
                select(view, true)
            }
        }
    }

    fun onChanged(action: ((T) -> Unit)? = null) {
        onChanged = action
    }

    private fun select(selected: View, isClick: Boolean = false) {
        if (selected.isSelected) return
        container.forEachIndexed { index, it ->
            it.isSelected = it == selected
            if (isClick && it.isSelected) {
                mValue = values[index]
                val newValue = mValue ?: return
                onChanged?.invoke(newValue)
            }
        }
    }
}