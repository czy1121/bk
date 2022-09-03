package com.demo.app.fragment

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.helper.OptionPickerHelper
import com.demo.app.R
import com.demo.app.databinding.FragmentShadowBinding
import me.reezy.cosmo.bk.BKTextView

class ShadowFragment() : Fragment(R.layout.fragment_shadow) {
    val binding by lazy { FragmentShadowBinding.bind(requireView()) }

    val bkTextView: BKTextView by lazy { requireActivity().findViewById(R.id.button) }

    private val Float.dp: Float get() = resources.displayMetrics.density * this

    private val spShadowColor by lazy {
        val teal200 = resources.getColor(R.color.teal_200, requireContext().theme)
        val purple200 = resources.getColor(R.color.purple_200, requireContext().theme)
        val red = resources.getColor(android.R.color.holo_red_light, requireContext().theme)
        val values = arrayOf(Color.TRANSPARENT, teal200, purple200, red)
        OptionPickerHelper(binding.lytShadowColor, values)
    }

    private val spShadowRadius by lazy {
        OptionPickerHelper(binding.lytShadowRadius, arrayOf(0f, 5f.dp, 10f.dp, 20f.dp))
    }

    private val spShadowPadding by lazy {
        OptionPickerHelper(binding.lytShadowPadding, arrayOf(0f, 5f.dp, 10f.dp, 20f.dp))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spShadowColor.value = Color.TRANSPARENT
        spShadowColor.onChanged {
            bkTextView.bk.shadowColor = it
        }

        spShadowRadius.value = 0f
        spShadowRadius.onChanged {
            bkTextView.bk.shadowRadius = it
        }

        spShadowPadding.value = 0f
        spShadowPadding.onChanged {
            val pad = it.toInt()
            bkTextView.bk.shadowPadding = Rect(pad, pad, pad, pad)
            bkTextView.setPaddingRelative(pad, pad, pad, pad)
        }


    }

}