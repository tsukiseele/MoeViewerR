package com.tsukiseele.moeviewerr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.miguelcatalan.materialsearchview.MaterialSearchView.*
import com.tsukiseele.moeviewerr.MainActivity
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.dataholder.TagHolder
import com.tsukiseele.moeviewerr.interfaces.OnMenuCreateListener
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseFragment
import com.tsukiseele.moeviewerr.ui.fragments.abst.SitePagerFragment
import com.tsukiseele.moeviewerr.ui.view.SearchView
import com.tsukiseele.moeviewerr.utils.OkHttpUtil
import es.dmoral.toasty.Toasty
import java.io.IOException
import java.util.*

class SiteContainerFragment : BaseFragment {
    private var toolbar: Toolbar? = null
    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
        private set
    var adapter: TabContainerViewPagerAdapter? = null
        private set
    var searchView: SearchView? = null
        private set
    private var currentFragment: SitePagerFragment? = null

    private var fragments: MutableList<SitePagerFragment> = arrayListOf()

    var onMenuCreateListener: OnMenuCreateListener = object : OnMenuCreateListener {
        override fun onMenuCreate() {
            val mainActivity = context as? MainActivity ?: return
            mainActivity.menuSearchItem ?: return
            // 初始化顶部搜索栏
            searchView?.apply {
                setMenuItem(mainActivity.menuSearchItem)
                setVoiceSearch(false)
//                setCursorDrawable(R.drawable.color_cursor_black);
                setEllipsize(true)
                setSuggestions(TagHolder.instance.allStringTag)
                setOnSearchViewListener(object : SearchViewListener {
                    override fun onSearchViewClosed() {}
                    override fun onSearchViewShown() {
                        setSuggestions(TagHolder.instance.allStringTag)
                    }
                })
                setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        try {
                            (currentFragment as GalleryFragment).search(query)
                            searchView!!.closeSearch()
                        } catch (e: Exception) {
                            Toasty.info(context!!, "该站点不支持搜索").show()
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        return false
                    }
                })
            }
        }
    }

    override val layoutId: Int
        get() = R.layout.fragment_site_container

    constructor() {
        this.fragments = arrayListOf()
    }

    constructor(fragments: MutableList<SitePagerFragment>) {
        this.fragments = fragments
    }

    override fun onCreateView(container: View, savedInstanceState: Bundle?) {
        toolbar = container.findViewById(R.id.fragmentTabContainer_Toolbar)
        tabLayout = container.findViewById(R.id.fragmentTabContainer_TabLayout)
        viewPager = container.findViewById(R.id.fragmentTabContainer_ViewPager)
        searchView = container.findViewById(R.id.fragmentTabContainer_MaterialSearchView)

        (context as MainActivity).onMenuCreateListener = onMenuCreateListener
        initToolbar(container)

        adapter = TabContainerViewPagerAdapter(fragmentManager!!)
        viewPager!!.adapter = adapter

        tabLayout!!.setSelectedTabIndicatorColor(-0x1)
        tabLayout!!.setTabTextColors(0x7FFFFFFF, -0x1)
        //tabLayout.setSelectedTabIndicatorHeight();

        tabLayout!!.setupWithViewPager(viewPager, false)
        // 必须要在tabLayout完成同步并设置适配器后调用
        adapter!!.setTabs(fragments)

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(pos: Int, offset: Float, p3: Int) {
                val fragments = getFragments()
                // 0 ~ 1，其中0表示滚动完毕，否则表示滚动的比例
                if (offset == 0f) {
                    currentFragment = fragments!![pos]
                    currentFragment!!.onDisplay()

                    if (pos - 1 >= 0)
                        fragments[pos - 1].onHide()
                    if (pos + 1 < fragments.size)
                        fragments[pos + 1].onHide()
                }
            }

            override fun onPageSelected(p1: Int) {

            }

            override fun onPageScrollStateChanged(p1: Int) {

            }
        })
    }

    override fun onStart() {
        super.onStart()
        (context as MainActivity).openDefaultSite()
    }


    // 初始化工具栏
    private fun initToolbar(layout: View) {
        // 同步DrawerLayout的动画
        val activity = context as MainActivity
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        // 初始化标题抽屉动画 ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(activity, activity.drawerLayout, toolbar, 0, 0)
        activity.drawerLayout!!.setDrawerListener(toggle)
        toggle.syncState()
        // 设置标题
        val tvTitle = layout.findViewById<TextView>(R.id.fragmentTabContainerTitle_TextView)
        val tvSubtitle = layout.findViewById<TextView>(R.id.fragmentTabContainerSubtitle_TextView)
        tvTitle.text = "画廊"
        tvSubtitle.text = "お帰りなさい"
        tvSubtitle.isSelected = true
        // 调戏一言接口
        Timer().schedule(object : TimerTask() {
            override fun run() {
                try {
                    val type = "abdi"
                    val json = OkHttpUtil["https://v1.hitokoto.cn/?c="
                            + type[(Math.random() * type.length).toInt()]
                            + "&encode=json"].body()!!.string()
                    val obj = JsonParser().parse(json) as JsonObject
                    val hitokoto = obj.get("hitokoto").asString
                    val from = obj.get("from").asString
                    val subtitle = "$hitokoto — $from"
                    activity.runOnUiThread { tvSubtitle.text = subtitle }
                } catch (e: IOException) {

                }
            }
        }, 0, 60000)
    }

    override fun onDestroyView() {
        val tabs = arrayOfNulls<String>(getFragments()!!.size)

        for (i in 0 until getFragments()!!.size)
            tabs[i] = getFragments()!![i].title
        GlobalObjectHolder.put("tabs", tabs)
        if (currentFragment != null) {
            GlobalObjectHolder.put(
                PreferenceHolder.KEY_LASTOPEN_SITE,
                adapter!!.getItemPosition(currentFragment!!)
            )
        }
        super.onDestroyView()
    }

    fun getFragments(): List<SitePagerFragment>? {
        return adapter!!.getFragments()
    }

    inner class TabContainerViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var fragments: MutableList<SitePagerFragment> = ArrayList()

        fun getFragments(): List<SitePagerFragment>? {
            return fragments
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(index: Int): Fragment {
            return fragments[index]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragments[position].title
        }

        fun addTab(fragment: SitePagerFragment) {
            loadTabView(fragment)
            fragments.add(fragment)
            notifyDataSetChanged()
        }

        fun addTabs(fragments: List<SitePagerFragment>) {
            loadTabViews(fragments)
            this.fragments.addAll(fragments)
            notifyDataSetChanged()
        }

        // 加载自定义Tab视图
        private fun loadTabView(fragment: SitePagerFragment) {
            // 自定义Tab
            val tab = tabLayout!!.newTab()
                .setCustomView(LayoutInflater.from(context).inflate(R.layout.view_tab_layout, null))
            val tvText = tab.customView!!.findViewById<TextView>(R.id.viewTabLayout_TextView)
            // 不使用删除按钮
            //ImageView ivClose = tab.getCustomView().findViewById(R.id.viewTabLayout_ImageView);
            tab.text = fragment.title
            tvText.text = fragment.title.toUpperCase()
            (tab.customView!!.parent as View).setOnLongClickListener {
                removeTab(fragment)
            }
            tabLayout!!.addTab(tab)
        }

        // 加载多个自定义Tab视图
        private fun loadTabViews(fragments: List<SitePagerFragment>) {
            for (fragment in fragments)
                loadTabView(fragment)
        }

        fun removeTab(index: Int): SitePagerFragment {
            val fragment = fragments.removeAt(index)
            notifyDataSetChanged()
            tabLayout!!.removeTabAt(index)
            return fragment
        }


        fun removeTab(fragment: SitePagerFragment): Boolean {
            val tlTabLayout = tabLayout as TabLayout
            for (i in 0 until tlTabLayout.tabCount) {
                val tab = tlTabLayout.getTabAt(i)
                if (tab != null && tab.text == fragment.title) {
                    tlTabLayout.removeTab(tab)
                    break
                }
            }
            return fragments.remove(fragment).also { notifyDataSetChanged() }
        }

        fun clear() {
            fragments.clear()
            notifyDataSetChanged()
        }

        fun setTabs(fragments: MutableList<SitePagerFragment>) {
            this.fragments = fragments
            loadTabViews(fragments!!)
            notifyDataSetChanged()
        }

        override fun getItemPosition(obj: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

    companion object {

        @JvmOverloads
        fun create(
            fm: FragmentManager,
            id: Int,
            fragments: MutableList<SitePagerFragment> = ArrayList()
        ): SiteContainerFragment {
            val fragment = SiteContainerFragment(fragments)
            fm.beginTransaction()
                .replace(id, fragment)
                .commit()
            return fragment
        }
    }
}
