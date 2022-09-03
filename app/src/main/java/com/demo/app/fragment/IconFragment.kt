package com.demo.app.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentIconBinding
import me.reezy.cosmo.bk.BKTextView

class IconFragment() : Fragment(R.layout.fragment_icon) {
    val binding by lazy { FragmentIconBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }


    private val spGravity by lazy {
        OptionPickerHelper(binding.lytGravity, arrayOf(0x1, 0x2, 0x3, 0x4, 0x10, 0x20))
    }
    private val spIcon by lazy {
        OptionPickerHelper(binding.pickerIcon, arrayOf(0, R.drawable.ic_pic, R.mipmap.ic_a))
    }
    private val spTint by lazy {
        val teal200 = resources.getColor(R.color.teal_200, requireContext().theme)
        val teal700 = resources.getColor(R.color.teal_700, requireContext().theme)
        val purple200 = resources.getColor(R.color.purple_200, requireContext().theme)
        val purple500 = resources.getColor(R.color.purple_500, requireContext().theme)
        val purple700 = resources.getColor(R.color.purple_700, requireContext().theme)
        OptionPickerHelper(binding.pickerTint, arrayOf(Color.TRANSPARENT, teal200, purple200, purple500))
    }

    private val Float.dp: Float get() = resources.displayMetrics.density * this


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        spIcon.value = 0
        spIcon.onChanged {
            bkTextView.setIconResource(it)
        }

        spTint.value = Color.TRANSPARENT
        spTint.onChanged {
            bkTextView.iconTint = if (it != 0) ColorStateList.valueOf(it) else null
        }

        spGravity.value = bkTextView.iconGravity
        spGravity.onChanged {
            bkTextView.iconGravity = it
        }
        binding.pickerSize.helper.onChanged {
            bkTextView.iconSize = it.dp.toInt()
        }
        binding.pickerPadding.helper.onChanged {
            bkTextView.iconPadding = it.dp.toInt()
        }


    }

}