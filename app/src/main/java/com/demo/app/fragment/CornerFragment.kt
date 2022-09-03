package com.demo.app.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentCornerBinding
import me.reezy.cosmo.bk.BKDrawable
import me.reezy.cosmo.bk.BKTextView

class CornerFragment() : Fragment(R.layout.fragment_corner) {
    val binding by lazy { FragmentCornerBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }


    private val Float.dp: Float get() = resources.displayMetrics.density * this

    private val spCornerRadius by lazy {
        OptionPickerHelper(binding.pickerCornerRadius, arrayOf(-1f, 0f, 5f.dp, 10f.dp, 20f.dp))
    }

    private val spCornerPosition by lazy {
        val values = arrayOf(
            BKDrawable.CORNERS_NORMAL,
            BKDrawable.CORNERS_DIAGONAL_DOWNWARD,
            BKDrawable.CORNERS_DIAGONAL_UPWARD,

            BKDrawable.CORNERS_TOP_LEFT,
            BKDrawable.CORNERS_TOP_RIGHT,
            BKDrawable.CORNERS_BOTTOM_LEFT,
            BKDrawable.CORNERS_BOTTOM_RIGHT,

            BKDrawable.CORNERS_SIDE_LEFT,
            BKDrawable.CORNERS_SIDE_RIGHT,
            BKDrawable.CORNERS_SIDE_TOP,
            BKDrawable.CORNERS_SIDE_BOTTOM,
        )
        OptionPickerHelper(binding.lytCornerPosition, values)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spCornerRadius.value = 0f
        spCornerRadius.onChanged {
            if (it < 0) {
                bkTextView.bk.isAutoCornerRadius = true
            } else {
                bkTextView.bk.isAutoCornerRadius = false
                bkTextView.bk.cornerRadius = it
            }
        }

        spCornerPosition.value = BKDrawable.CORNERS_NORMAL
        spCornerPosition.onChanged {
            bkTextView.bk.cornerPosition = it
        }

    }

}