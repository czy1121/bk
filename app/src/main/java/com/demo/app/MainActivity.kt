package com.demo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.demo.app.databinding.ActivityMainBinding
import com.demo.app.fragment.*
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        val tabItems = arrayOf("背景", "渐变", "圆角", "阴影", "描边", "图标")

        binding.pager.adapter = FragmentAdapter(this, tabItems) { _, pos ->
            when (pos) {
                0 -> BackgroundFragment()
                1 -> GradientFragment()
                2 -> CornerFragment()
                3 -> ShadowFragment()
                4 -> StrokeFragment()
                else -> IconFragment()
            }
        }

        binding.pager.offscreenPageLimit = 5

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = tabItems[position]
        }.attach()

        binding.tabs.getTabAt(0)?.select()
    }

}