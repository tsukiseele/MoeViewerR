package com.tsukiseele.moeviewerr.libraries

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

/**
 * Created by codeest on 2016/8/24.
 * http://blog.csdn.net/yanzhenjie1003/article/details/51935982
 */

class DefaultItemTouchHelpCallback(
    /**
     * Item操作的回调
     */
    private var onItemTouchCallbackListener: OnItemTouchCallbackListener?
) : ItemTouchHelper.Callback() {

    /**
     * 是否可以拖拽
     */
    private var isCanDrag = true
    /**
     * 是否可以被滑动
     */
    private var isCanSwipe = true

    /**
     * 设置Item操作的回调，去更新UI和数据源
     *
     * @param onItemTouchCallbackListener
     */
    fun setOnItemTouchCallbackListener(onItemTouchCallbackListener: OnItemTouchCallbackListener) {
        this.onItemTouchCallbackListener = onItemTouchCallbackListener
    }

    /**
     * 设置是否可以被拖拽
     *
     * @param canDrag 是true，否false
     */
    fun setDragEnable(canDrag: Boolean) {
        isCanDrag = canDrag
    }

    /**
     * 设置是否可以被滑动
     *
     * @param canSwipe 是true，否false
     */
    fun setSwipeEnable(canSwipe: Boolean) {
        isCanSwipe = canSwipe
    }

    /**
     * 当Item被长按的时候是否可以被拖拽
     *
     * @return
     */
    override fun isLongPressDragEnabled(): Boolean {
        return isCanDrag
    }

    /**
     * Item是否可以被滑动(H：左右滑动，V：上下滑动)
     *
     * @return
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return isCanSwipe
    }

    /**
     * 当用户拖拽或者滑动Item的时候需要我们告诉系统滑动或者拖拽的方向
     *
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {// GridLayoutManager
            // flag如果值是0，相当于这个功能被关闭
            val dragFlag =
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlag = 0
            // create make
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlag, swipeFlag)
        } else if (layoutManager is LinearLayoutManager) {// linearLayoutManager
            val linearLayoutManager = layoutManager as LinearLayoutManager?
            val orientation = linearLayoutManager!!.orientation

            var dragFlag = 0
            var swipeFlag = 0

            // 为了方便理解，相当于分为横着的ListView和竖着的ListView
            if (orientation == LinearLayoutManager.HORIZONTAL) {// 如果是横向的布局
                swipeFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                dragFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            } else if (orientation == LinearLayoutManager.VERTICAL) {// 如果是竖向的布局，相当于ListView
                dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                swipeFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            }
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlag, swipeFlag)
        }
        return 0
    }

    /**
     * 当Item被拖拽的时候被回调
     *
     * @param recyclerView     recyclerView
     * @param srcViewHolder    拖拽的ViewHolder
     * @param targetViewHolder 目的地的viewHolder
     * @return
     */
    override fun onMove(
        recyclerView: RecyclerView,
        srcViewHolder: RecyclerView.ViewHolder,
        targetViewHolder: RecyclerView.ViewHolder
    ): Boolean {
        return if (onItemTouchCallbackListener != null) {
            onItemTouchCallbackListener!!.onMove(
                srcViewHolder.adapterPosition,
                targetViewHolder.adapterPosition
            )
        } else false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (onItemTouchCallbackListener != null) {
            onItemTouchCallbackListener!!.onSwiped(viewHolder.adapterPosition)
        }
    }

    interface OnItemTouchCallbackListener {
        /**
         * 当某个Item被滑动删除的时候
         *
         * @param adapterPosition item的position
         */
        fun onSwiped(adapterPosition: Int)

        /**
         * 当两个Item位置互换的时候被回调
         *
         * @param srcPosition    拖拽的item的position
         * @param targetPosition 目的地的Item的position
         * @return 开发者处理了操作应该返回true，开发者没有处理就返回false
         */
        fun onMove(fromPosition: Int, toPosition: Int): Boolean
    }
}
