package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.FavoritesHolder
import com.tsukiseele.moeviewerr.interfaces.CustiomItemClickListener
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.ui.activitys.CatalogActivity
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter
import com.tsukiseele.moeviewerr.ui.activitys.ImageViewerActivity
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.moeviewerr.utils.ActivityUtil
import com.tsukiseele.moeviewerr.utils.startActivityOfFadeAnimation
import kotlinx.android.synthetic.main.fragment_favorite.*

class FavoriteFragment : BaseMainFragment() {
    override val title: String
        get() = "收藏"
    override val layoutId: Int
        get() = R.layout.fragment_favorite

    private var mAdapter: ImageGridAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ImageGridAdapter(context!!, FavoritesHolder.getImages())
        fragmentKira_RecyclerView.layoutManager = GridLayoutManager(context, 3)
        fragmentKira_RecyclerView.isScrollbarFadingEnabled = true
        fragmentKira_RecyclerView.adapter = mAdapter

        fragmentKira_SwipeRefreshLayout.setColorSchemeResources(*App.swipeRefreshColors!!)
        fragmentKira_SwipeRefreshLayout.setProgressViewOffset(true, -100, 50)
        fragmentKira_SwipeRefreshLayout.setOnRefreshListener {
            mAdapter?.updateDataSet(FavoritesHolder.getImages())
            fragmentKira_SwipeRefreshLayout.isRefreshing = false
        }

        mAdapter?.setOnItemClickListener(object : CustiomItemClickListener() {
            override fun onItemSingleClick(view: View, position: Int) {
                mAdapter?.list?.get(position)?.let {
                    if (it.hasCatalog()) {
                        GlobalObjectHolder.put("image_catalog", it)
                        val intent = Intent(context, CatalogActivity::class.java)
                        ActivityUtil.startActivityOfSceneTransition(activity!!, view.findViewById(R.id.itemListImageGridLayoutCover_ImageView), intent)
                    } else {
                        GlobalObjectHolder.put("images", mutableListOf(it))
                        GlobalObjectHolder.put("images_index", position)
                        val intent = Intent(context, ImageViewerActivity::class.java)
                        activity?.startActivityOfFadeAnimation(intent)
                    }
                }
            }
        })
        mAdapter?.setOnLongItemClickListener(object : BaseAdapter.OnLongItemClickListener {
            override fun onLongItemClick(view: View, position: Int) {
                Snackbar.make(view, "要移除它吗?", Snackbar.LENGTH_LONG)
                    .setAction("确定", {
                        mAdapter?.list?.getOrNull(position)?.also {
                            FavoritesHolder.remove(it)
                            FavoritesHolder.saveState()
                            mAdapter?.updateDataSet(FavoritesHolder.getImages())
                        }
                    })
                    .setActionTextColor(resources.getColor(R.color.primary))
                    .show()
            }
        })
    }
}
