package com.tsukiseele.moeviewerr.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.miguelcatalan.materialsearchview.MaterialSearchView

/**
 * 继承于MaterialSearchView，仅重写搜索判断代码，使其允许搜索空串
 */
class SearchView : MaterialSearchView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val editTextField = MaterialSearchView::class.java.getDeclaredField("mSearchSrcTextView")
        val onQueryTextListenerField =
            MaterialSearchView::class.java.getDeclaredField("mOnQueryChangeListener")
        editTextField.isAccessible = true
        onQueryTextListenerField.isAccessible = true
        (editTextField.get(this) as EditText).let {
            it.setOnEditorActionListener { v, actionId, event ->
                val listener = onQueryTextListenerField.get(this) as OnQueryTextListener
                listener.onQueryTextSubmit(it.text.toString())
                closeSearch()
                true
            }
        }
    }
}