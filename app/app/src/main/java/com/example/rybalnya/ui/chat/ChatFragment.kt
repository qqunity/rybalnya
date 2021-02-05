package com.example.rybalnya.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.rybalnya.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ChatFragment : Fragment() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.addFragment(TalksFragment(), "Чаты")
        viewPagerAdapter.addFragment(FriendsFragment(), "Пользователи")
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = viewPagerAdapter
        val tabLayout: TabLayout = view.findViewById(R.id.chatTabs)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getPageTitle(position)
        }.attach()

    }


}

class ViewPagerAdapter(
    fragment: Fragment,
    private var fragments: ArrayList<Fragment> = ArrayList(),
    private var titles: ArrayList<String> = ArrayList()
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        /*        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, position + 1)
        }*/
        return fragments[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
    }

    fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

private const val ARG_OBJECT = "object"