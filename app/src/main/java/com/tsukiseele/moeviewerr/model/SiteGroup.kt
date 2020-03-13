package com.tsukiseele.moeviewerr.model

import com.hgdendi.expandablerecycleradapter.BaseExpandableRecyclerViewAdapter
import com.tsukiseele.sakurawler.model.Site

class SiteGroup(val title: String, val sites: ArrayList<Site>) :
    BaseExpandableRecyclerViewAdapter.BaseGroupBean<Site> {

    override fun getChildCount(): Int {
        return sites.size
    }

    override fun getChildAt(childIndex: Int): Site? {
        return if (childIndex < 0 || childIndex >= sites.size) null else sites[childIndex]
    }

    override fun isExpandable(): Boolean {
        return !sites.isEmpty()
    }
}
