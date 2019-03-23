package com.vincent.forexmgt.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class EntryPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    private val titles: MutableList<String> = mutableListOf()
    private val fragments: MutableList<Fragment> = mutableListOf()

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    fun addFragment(title: String, fragment: Fragment) {
        titles.add(title)
        fragments.add(fragment)
    }
}