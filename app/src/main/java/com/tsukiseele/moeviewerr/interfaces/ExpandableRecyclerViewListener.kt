package com.tsukiseele.moeviewerr.interfaces

import com.hgdendi.expandablerecycleradapter.BaseExpandableRecyclerViewAdapter

interface ExpandableRecyclerViewListener<GroupBean : BaseExpandableRecyclerViewAdapter.BaseGroupBean<ChildBean>, ChildBean> :
    BaseExpandableRecyclerViewAdapter.ExpandableRecyclerViewOnClickListener<GroupBean, ChildBean> {
    fun onChildLongClicked(group: GroupBean, child: ChildBean): Boolean
}