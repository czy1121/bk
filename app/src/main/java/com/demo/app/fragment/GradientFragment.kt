package com.demo.app.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentGradientBinding
import me.reezy.cosmo.bk.BKTextView

class GradientFragment() : Fragment(R.layout.fragment_gradient) {
    val binding by lazy { FragmentGradientBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }


    private val spOrientation by lazy {
        OptionPickerHelper(binding.lytOrientation, arrayOf(0, 1, 2, 3, 4, 5, 6, 7))
    }
    private val spType by lazy {
        OptionPickerHelper(binding.pickerType, arrayOf(0, 1, 2))
    }
    private val spColors by lazy {
        val teal200 = resources.getColor(R.color.teal_200, requireContext().theme)
        val teal700 = resources.getColor(R.color.teal_700, requireContext().theme)
        val purple200 = resources.getColor(R.color.purple_200, requireContext().theme)
        val purple500 = resources.getColor(R.color.purple_500, requireContext().theme)
        val purple700 = resources.getColor(R.color.purple_700, requireContext().theme)
        val values = arrayOf(
            intArrayOf(),
            intArrayOf(teal200, teal700),
            intArrayOf(purple200, purple500, purple700)
        )
        OptionPickerHelper(binding.pickerColors, values)
    }

    private val Float.dp: Float get() = resources.displayMetrics.density * this


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spColors.value = null
        spColors.onChanged {
            bkTextView.bk.gradientColors = it
        }

        spType.value = 0
        spType.onChanged {
            bkTextView.bk.gradientType = it
        }

        spOrientation.value = 0
        spOrientation.onChanged {
            bkTextView.bk.gradientOrientation = try {
                GradientDrawable.Orientation.values()[it]
            } catch (e: IndexOutOfBoundsException) {
                GradientDrawable.Orientation.TOP_BOTTOM
            }
        }

        binding.pickerCenterX.helper.onChanged {
            bkTextView.bk.gradientCenterX = it
        }
        binding.pickerCenterY.helper.onChanged {
            bkTextView.bk.gradientCenterY = it
        }
        binding.pickerRadius.helper.onChanged {
            bkTextView.bk.gradientRadius = it.dp
        }


    }

}