package com.demo.app.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentStrokeBinding
import me.reezy.cosmo.bk.BKTextView
import me.reezy.cosmo.bk.bkColorStateList

class StrokeFragment() : Fragment(R.layout.fragment_stroke) {
    val binding by lazy { FragmentStrokeBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }

    private val Float.dp: Float get() = resources.displayMetrics.density * this

    val TRANSPARENT = ColorStateList.valueOf(Color.TRANSPARENT)

    private val spStrokeColor by lazy {
        val black = resources.getColor(R.color.black, requireContext().theme)
        val orange = resources.getColor(android.R.color.holo_orange_dark, requireContext().theme)
        val purple500 = resources.getColor(R.color.purple_500, requireContext().theme)
        val values = arrayOf(
            TRANSPARENT,
            ColorStateList.valueOf(black),
            ColorStateList.valueOf(orange),
            bkColorStateList(black, orange, purple500, 0x80000000.toInt())!!
        )
        OptionPickerHelper(binding.lytStrokeColor, values)
    }

    private val spStrokeWidth by lazy {
        OptionPickerHelper(binding.lytStrokeWidth, arrayOf(0f, 1f.dp, 2f.dp, 4f.dp))
    }

    private val spStrokeDashWidth by lazy {
        OptionPickerHelper(binding.lytStrokeDashWidth, arrayOf(0f, 2f.dp, 4f.dp, 8f.dp))
    }

    private val spStrokeDashGap by lazy {
        OptionPickerHelper(binding.lytStrokeDashGap, arrayOf(0f, 2f.dp, 4f.dp, 8f.dp))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spStrokeColor.value = TRANSPARENT
        spStrokeColor.onChanged {
            bkTextView.bk.strokeColor = it
        }

        spStrokeWidth.value = 0f
        spStrokeWidth.onChanged {
            bkTextView.bk.strokeWidth = it
        }

        spStrokeDashWidth.value = 0f
        spStrokeDashWidth.onChanged {
            bkTextView.bk.setStrokeDash(it, spStrokeDashGap.value ?: 0f)
        }

        spStrokeDashGap.value = 0f
        spStrokeDashGap.onChanged {
            bkTextView.bk.setStrokeDash(spStrokeDashWidth.value ?: 0f, it)
        }


    }

}