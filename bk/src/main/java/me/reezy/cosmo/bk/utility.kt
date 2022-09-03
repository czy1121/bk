package me.reezy.cosmo.bk

import android.content.res.ColorStateList


fun bkColorStateList(default: Int? = null, selected: Int? = null, pressed: Int? = null, disabled: Int? = null): ColorStateList? {
    return when {
        default == null && selected == null && pressed == null && disabled == null -> null
        else -> {
            val states = arrayOf(
                intArrayOf(-android.R.attr.state_enabled) to disabled,
                intArrayOf(android.R.attr.state_selected) to selected,
                intArrayOf(android.R.attr.state_pressed) to pressed,
                intArrayOf() to default
            ).filter { it.second != null }
            ColorStateList(states.map { it.first }.toTypedArray(), states.map { it.second!! }.toIntArray())
        }
    }
}