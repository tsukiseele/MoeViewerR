package com.tsukiseele.moeviewerr

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonSyntaxException
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.dataholder.DownloadHolder
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder.KEY_LASTOPEN_SITE
import com.tsukiseele.moeviewerr.interfaces.ExpandableRecyclerViewListener
import com.tsukiseele.moeviewerr.interfaces.OnMenuCreateListener
import com.tsukiseele.moeviewerr.model.SiteGroup
import com.tsukiseele.moeviewerr.ui.activitys.AboutActivity
import com.tsukiseele.moeviewerr.ui.activitys.SettingsActivity
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseActivity
import com.tsukiseele.moeviewerr.ui.adapter.DrawerRightTreeAdapter
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter
import com.tsukiseele.moeviewerr.ui.adapter.ImageStaggeredAdapter
import com.tsukiseele.moeviewerr.ui.fragments.*
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.moeviewerr.ui.fragments.abst.SitePagerFragment
import com.tsukiseele.moeviewerr.utils.Util
import com.tsukiseele.moeviewerr.utils.startActivityOfFadeAnimation
import com.tsukiseele.sakurawler.SiteManager
import com.tsukiseele.sakurawler.model.Site
import es.dmoral.toasty.Toasty
import me.shihao.library.XStatusBarHelper
import java.util.*
import java.util.regex.Pattern

class MainActivity : BaseActivity() {
    private val LISTENER = Listener()
    // 左抽屉View
    var drawerLayout: DrawerLayout? = null
    private var drawerLeftNavigationView: NavigationView? = null
    // 右抽屉View
    private var drawerRightListView: RecyclerView? = null
    var drawerRightTreeAdapter: DrawerRightTreeAdapter? = null
        get() = field
        private set
    // Tab容器
    private var siteContainer: SiteContainerFragment? = null
    // 当前抽屉菜单项id
    private var currentDrawerItemId: Int = 0
    // 当前使用的站点规则
    private var currentSite: Site? = null
    // 菜单搜索项
    var menuSearchItem: MenuItem? = null
        private set
    // 菜单创建监听
    var onMenuCreateListener: OnMenuCreateListener? = null
    // 全局变量
    private var exitTimer: Long = 0
    private var dialog: AlertDialog? = null

    internal inner class Listener :
        ExpandableRecyclerViewListener<SiteGroup, Site> {

        override fun onGroupLongClicked(groupItem: SiteGroup): Boolean {
            return false
        }

        override fun onInterceptGroupExpandEvent(groupItem: SiteGroup, isExpand: Boolean): Boolean {
            return false
        }

        override fun onGroupClicked(groupItem: SiteGroup) {}

        override fun onChildClicked(groupItem: SiteGroup, childItem: Site) {
            openSite(childItem)
        }

        override fun onChildLongClicked(groupItem: SiteGroup, childItem: Site): Boolean {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("站点信息")
                .setMessage("站点名: " + childItem.title + "\n描述: " + childItem.remarks)
                .setNeutralButton("源网站") { dialog, p2 ->
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(childItem.homeSection!!.indexUrl)
                    this@MainActivity.startActivityOfFadeAnimation(intent)
                }
                .setPositiveButton("确定", null)
                .show()
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 每次启动都会初始化目录配置
        Config.initAppDirectory()

        setContentView(R.layout.activity_main)
        drawerLayout = this.findViewById(R.id.activityMain_DrawerLayout)
        drawerRightListView = this.findViewById(R.id.drawerRightLayout_RecyclerView)
        // 初始化左抽屉
        drawerLeftNavigationView = this.findViewById(R.id.activityMainDrawerLeft_NavigationView)
        drawerLeftNavigationView?.setCheckedItem(R.id.menuDrawerSource)
        drawerLeftNavigationView?.setNavigationItemSelectedListener(DrawerLeftMenuListener())
        // 初始化右抽屉
        drawerRightTreeAdapter = DrawerRightTreeAdapter(SiteManager.getSiteMap())
        drawerRightListView?.layoutManager = LinearLayoutManager(this)
        drawerRightListView?.adapter = drawerRightTreeAdapter
        drawerRightTreeAdapter?.expandableRecyclerViewListener = LISTENER
        siteContainer = SiteContainerFragment.create(supportFragmentManager, ID_CONTAINER)
        // 设置沉浸式状态栏
        XStatusBarHelper.tintStatusBarForDrawer(
            this,
            drawerLayout,
            getResources().getColor(R.color.primary),
            0F
        )
        // 设置左右抽屉的滑动范围
        Util.setDrawerEdgeSize(this, drawerLayout, "Left", 0.15f)
        Util.setDrawerEdgeSize(this, drawerLayout, "Right", 0.15f)
        // 检查插件错误
        checkSiteLoadError()
        // 绑定下载服务
        DownloadHolder.bind(this)
    }


    override fun onStart() {
        super.onStart()
        openDefaultSite()
    }

    fun openDefaultSite() {
        // 默认选中一个
        if (siteContainer!!.adapter!!.count == 0) {
            if (drawerRightTreeAdapter!!.groupCount != 0) {
                val group = drawerRightTreeAdapter!!.getGroupItem(0)
                drawerRightTreeAdapter!!.expandGroup(group)
                // 试图恢复最后打开的Tab
                val title = PreferenceHolder.getString(KEY_LASTOPEN_SITE, "")
                if (title == null) {
                    openSite(group.getChildAt(0)!!)
                } else {
                    val site = SiteManager.findSiteByTitle(title)
                    if (site == null)
                        openSite(group.getChildAt(0)!!)
                    else
                        openSite(site)
                }
            } else {
                Toasty.warning(this, "您还没有订阅任何站点，请添加订阅", Toasty.LENGTH_LONG).show()
            }
        }
    }

    private fun openSite(site: Site) {
        PreferenceHolder.putString(KEY_LASTOPEN_SITE, site.title!!)
        currentSite = site
        drawerLayout!!.closeDrawers()

        if (siteContainer == null)
            siteContainer = SiteContainerFragment.create(supportFragmentManager, ID_CONTAINER)
        // 查找选定的Tab是否存在，若存在则定位到选定的Tab
        val fragments = siteContainer!!.adapter!!.getFragments()
        for (i in fragments!!.indices) {
            val fragment = fragments[i]
            if (site.title == fragment.title) {
                siteContainer!!.viewPager!!.currentItem = i
                return
            }
        }
        // 添加Tab，并更新到当前Tab
        siteContainer!!.adapter!!.addTab(GalleryFragment(site))
        siteContainer!!.viewPager!!.currentItem = fragments.size
    }

    private fun checkSiteLoadError() {
        val exces = SiteManager.getErrorMessage()
        if (exces.isEmpty())
            return
        val siteTitles = exces.keys.toTypedArray()

        val close = DialogInterface.OnClickListener { p1, p2 ->
            dialog!!.cancel()
            dialog = null
        }
        dialog = AlertDialog.Builder(this)
            .setTitle("以下规则加载失败：")
            .setItems(siteTitles) { p1, pos ->
                val sb = StringBuilder()
                val e = exces[siteTitles[pos]]
                if (e is JsonSyntaxException) {
                    val pattern = Pattern.compile("line (\\d+) mListColumn (\\d+)")
                    val matcher = pattern.matcher(e.toString())
                    if (matcher.find())
                        sb.append(
                            String.format(
                                "\nJSON语法错误：行 %s, 列 %s\n",
                                matcher.group(1),
                                matcher.group(2)
                            )
                        ).append('\n')
                }
                sb.append(e!!.toString())
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(siteTitles[pos])
                    .setMessage(sb.toString())
                    .setNegativeButton("返回列表") { p1, p2 -> dialog!!.show() }
                    .setPositiveButton("关闭", close)
                    .show()
            }
            .setNegativeButton("跳过", close)
            .create()
        dialog!!.show()
    }

    // Menu 初始化
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (App.packageInfo!!.versionName != null)
            menu.getItem(0).title = "版本：" + App.packageInfo!!.versionName
        // 获取到菜单搜索项
        menuSearchItem = menu.findItem(R.id.menuSearch)
        // 通知创建
        if (onMenuCreateListener != null)
            onMenuCreateListener!!.onMenuCreate()
        return true
    }

    // Toolbar Menu改变
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (currentDrawerItemId) {
            R.id.menuDrawerCollentions,
            R.id.menuDrawerTags,
            R.id.menuDrawerSubscribe,
            R.id.menuDrawerDownloadManager ->
                menu.findItem(R.id.menuSearch).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    // Toolbar Menu点击监听
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuVersion -> if (true) {
                Toasty.info(this, "当前已是最新版本").show()
            }
            R.id.menuListType -> AlertDialog.Builder(this)
                .setTitle("列表类型")
                .setItems(arrayOf("瀑布流 - 2列", "瀑布流 - 3列", "网格 - 3列")) { di, pos ->
                    when (pos) {
                        0 -> PreferenceHolder.putInt(
                            PreferenceHolder.KEY_LISTTYPE,
                            ImageStaggeredAdapter.TYPE_FLOW_2_COL
                        )
                        1 -> PreferenceHolder.putInt(
                            PreferenceHolder.KEY_LISTTYPE,
                            ImageStaggeredAdapter.TYPE_FLOW_3_COL
                        )
                        2 -> PreferenceHolder.putInt(
                            PreferenceHolder.KEY_LISTTYPE,
                            ImageGridAdapter.TYPE_GRID_3_COL
                        )
                    }
                }
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    // 左抽屉菜单项点击监听
    private inner class DrawerLeftMenuListener : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
            currentDrawerItemId = menuItem.itemId
            when (currentDrawerItemId) {
                // 资源
                R.id.menuDrawerSource -> {
                    drawerLayout!!.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_UNLOCKED,
                        GravityCompat.END
                    )
                    val obj = GlobalObjectHolder.remove("tabs")
                    // 试图恢复之前打开的Tab
                    if (obj != null) {
                        val tabs = obj as Array<String>
                        val fs = ArrayList<SitePagerFragment>()
                        for (tab in tabs)
                            SiteManager.findSiteByTitle(tab)?.let {
                                fs.add(GalleryFragment(it))
                            }
                        siteContainer =
                            SiteContainerFragment.create(supportFragmentManager, ID_CONTAINER, fs)
                    } else {
                        siteContainer =
                            SiteContainerFragment.create(supportFragmentManager, ID_CONTAINER)
                    }
                }
                // 标签
                R.id.menuDrawerTags ->
                    replaceMainFragment(TagManagerFragment())
                // 收藏
                R.id.menuDrawerCollentions ->
                    replaceMainFragment(FavoriteFragment())
                // 订阅
                R.id.menuDrawerSubscribe ->
                    replaceMainFragment(SubscribeFragment())
                // 下载管理
                R.id.menuDrawerDownloadManager ->
                    replaceMainFragment(DownloadFragment())

                // 首选项
                R.id.menuDrawerPreference -> {
                    startActivityOfFadeAnimation(SettingsActivity::class.java)
                }
                // 关于
                R.id.menuDrawerAbout -> {
                    startActivityOfFadeAnimation(AboutActivity::class.java)
                }
                R.id.menuDrawerExit -> App.app?.exit()
            }
            drawerLayout?.closeDrawers()
            return true
        }
    }

    // 替换主碎片
    fun replaceMainFragment(fragment: BaseMainFragment) {
        drawerLayout!!.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
            GravityCompat.END
        )
        supportFragmentManager
            .beginTransaction()
            .replace(ID_CONTAINER, fragment)
            .commit()
    }

    // 虚拟按键操作
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        drawerLayout?.also {
            when (keyCode) {
                // 按下 MENU 键弹出抽屉
                KeyEvent.KEYCODE_MENU -> {
                    // 当前允许打开右抽屉
                    if (DrawerLayout.LOCK_MODE_UNLOCKED == it.getDrawerLockMode(GravityCompat.END)) {
                        // 右抽屉已打开，则关闭，否则打开
                        if (it.isDrawerOpen(GravityCompat.END))
                            it.closeDrawers()
                        else
                            it.openDrawer(GravityCompat.END)
                    }
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    // 搜索栏未关闭，则关闭搜索
                    siteContainer?.searchView?.also {
                        if (it.isSearchOpen) {
                            it.closeSearch()
                            return true
                        }
                    }
                    // 抽屉开启则关闭 否则退出
                    if (it.isDrawerOpen(GravityCompat.START) ||
                        it.isDrawerOpen(GravityCompat.END)
                    ) {
                        it.closeDrawers()
                        // 双击 BACK 键退出
                    } else if (System.currentTimeMillis() - exitTimer > 1500) {
                        exitTimer = System.currentTimeMillis()
                        Snackbar.make(it, "再按一次退出程序", Snackbar.LENGTH_SHORT).show()
                    } else {
                        App.app?.exit()
                    }
                    return true
                }
            }
        }
        // 一定要做完后返回 true，或者在弹出菜单后返回true，其他键返回super以将其默认
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private val ID_CONTAINER = R.id.activityMainContent_FrameLayout
    }
}
