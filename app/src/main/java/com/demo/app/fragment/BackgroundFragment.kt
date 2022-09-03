package com.demo.app.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentBackgroundBinding
import me.reezy.cosmo.bk.BKTextView
import me.reezy.cosmo.bk.bkColorStateList

class BackgroundFragment() : Fragment(R.layout.fragment_background) {
    val binding by lazy { FragmentBackgroundBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }

    val TRANSPARENT = ColorStateList.valueOf(Color.TRANSPARENT)


    private val spBackground by lazy {
        val teal200 = resources.getColor(R.color.teal_200, requireContext().theme)
        val teal700 = resources.getColor(R.color.teal_700, requireContext().theme)
        val purple200 = resources.getColor(R.color.purple_200, requireContext().theme)
        val values = arrayOf(
            TRANSPARENT,
            ColorStateList.valueOf(teal200),
            ColorStateList.valueOf(purple200),
            bkColorStateList(teal200, teal700, purple200, 0x80000000.toInt())!!
        )
        OptionPickerHelper(binding.pickerBackground, values)
    }

    private val Float.dp: Float get() = resources.displayMetrics.density * this


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spBackground.value = TRANSPARENT
        spBackground.onChanged {
            bkTextView.bk.backgroundColor = it
        }

        binding.switchSelected.setOnCheckedChangeListener { buttonView, isChecked ->
            bkTextView.isSelected = isChecked
        }

        binding.switchDisabled.setOnCheckedChangeListener { buttonView, isChecked ->
            bkTextView.isEnabled = !isChecked
        }


    }

}