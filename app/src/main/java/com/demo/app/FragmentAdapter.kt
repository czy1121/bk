package com.demo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter<T>(manager: FragmentManager, lifecycle: Lifecycle, private val items: Array<T>, private val factory: (item: T, pos:Int) -> Fragment) : FragmentStateAdapter(manager, lifecycle) {

    constructor(activity: FragmentActivity, items: Array<T>, factory: (T, Int) -> Fragment) : this(activity.supportFragmentManager, activity.lifecycle, items, factory)

    constructor(fragment: Fragment, items: Array<T>, factory: (T, Int) -> Fragment) : this(fragment.childFragmentManager, fragment.lifecycle, items, factory)

    override fun createFragment(position: Int): Fragment = factory(items[position], position)

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): T = items[position]
}