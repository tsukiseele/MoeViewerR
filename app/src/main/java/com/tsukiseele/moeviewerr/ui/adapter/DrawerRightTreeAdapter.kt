package com.tsukiseele.moeviewerr.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hgdendi.expandablerecycleradapter.BaseExpandableRecyclerViewAdapter
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.model.SiteGroup
import com.tsukiseele.moeviewerr.interfaces.ExpandableRecyclerViewListener
import com.tsukiseele.moeviewerr.utils.TextUtil
import com.tsukiseele.moeviewerr.utils.AndroidUtil
import com.tsukiseele.sakurawler.Sakurawler
import com.tsukiseele.sakurawler.model.Site

class DrawerRightTreeAdapter(siteMap: Map<String, MutableList<Site>>) : BaseExpandableRecyclerViewAdapter
        <SiteGroup, Site, DrawerRightTreeAdapter.SiteItemGroupVH, DrawerRightTreeAdapter.SiteItemVH>() {
    var siteGroups: List<SiteGroup> = ArrayList()
        private set
    var expandableRecyclerViewListener: ExpandableRecyclerViewListener<SiteGroup, Site>? = null
        get() = field
        set(value) {
            setListener(value)
            field = value
        }
    private val errors = ArrayList<String>()

    init {
        this.siteGroups = asSiteMapToSiteGroups(siteMap)
    }

    fun updateDataSet(siteGroups: List<SiteGroup>) {
        this.siteGroups = siteGroups
        notifyDataSetChanged()
    }

    fun updateDataSet(siteMap: Map<String, List<Site>>) {
        this.siteGroups = asSiteMapToSiteGroups(siteMap)
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int {
        return siteGroups.size
    }

    override fun getGroupItem(groupIndex: Int): SiteGroup {
        return siteGroups[groupIndex]
    }

    override fun onCreateGroupViewHolder(
        parent: ViewGroup,
        groupViewType: Int
    ): DrawerRightTreeAdapter.SiteItemGroupVH {
        return SiteItemGroupVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_tree_drawer_right_group, parent, false
            )
        )
    }

    override fun onBindGroupViewHolder(
        holder: DrawerRightTreeAdapter.SiteItemGroupVH,
        groupBean: SiteGroup,
        isExpand: Boolean
    ) {
        if (groupBean.isExpandable) {
            holder.ivState.visibility = View.VISIBLE
            holder.ivState.setImageResource(if (isExpand) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)
        } else {
            holder.ivState.visibility = View.INVISIBLE
        }

        holder.tvTitle.text = groupBean.title.toUpperCase()
    }

    override fun onCreateChildViewHolder(
        parent: ViewGroup,
        childViewType: Int
    ): DrawerRightTreeAdapter.SiteItemVH {
        return SiteItemVH(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_tree_drawer_right, parent, false
            )
        )
    }



    override fun onBindChildViewHolder(
        holder: DrawerRightTreeAdapter.SiteItemVH,
        groupBean: SiteGroup,
        site: Site
    ) {
        expandableRecyclerViewListener?.let {
            holder.itemView.setOnLongClickListener {
                expandableRecyclerViewListener?.onChildLongClicked(groupBean, site) ?: false
            }
        }
        holder.tvTitle.text = site.title!!.toUpperCase()
        holder.tvTitle.isSelected = true

        val iconUrl = site.icon
        if (TextUtil.isEmpty(iconUrl) || errors.contains(iconUrl)) {
            holder.ivIcon.setImageResource(R.drawable.ic_image_off)
        } else {
            // 导入请求头
            val url = AndroidUtil.buildGlideUrl(iconUrl!!, Sakurawler(site).headers)

            Glide.with(App.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image_off)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        errors.add(model.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        data: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(holder.ivIcon)
        }
        holder.itemView.setBackgroundColor(if (holder.adapterPosition % 2 == 0) -0x1 else -0x111112)
    }

    class SiteItemVH(view: View) : RecyclerView.ViewHolder(view) {
        var ivIcon: ImageView
        var tvTitle: TextView

        init {
            ivIcon = view.findViewById(R.id.itemDrawerRightTreeItemIcon_ImageView)
            tvTitle = view.findViewById(R.id.itemDrawerRightTreeItem_TextView)
        }
    }

    class SiteItemGroupVH(view: View) :
        BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder(view) {
        var ivState: ImageView
        var tvTitle: TextView

        init {
            ivState = view.findViewById(R.id.itemDrawerRightTreeGroupState_ImageView)
            tvTitle = view.findViewById(R.id.itemDrawerRightTreeGroup_TextView)
        }

        override fun onExpandStatusChanged(
            relatedAdapter: RecyclerView.Adapter<*>,
            isExpanding: Boolean
        ) {
            ivState.setImageResource(if (isExpanding) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)
        }
    }

    companion object {

        fun asSiteMapToSiteGroups(siteMap: Map<String, List<Site>>): List<SiteGroup> {
            val siteGroups = ArrayList<SiteGroup>()
            for ((key, value) in siteMap)
                siteGroups.add(SiteGroup(key, value as ArrayList<Site>))
            return siteGroups
        }
    }
}
