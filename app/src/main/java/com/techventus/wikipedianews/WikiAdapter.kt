package com.techventus.wikipedianews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.techventus.wikipedianews.logging.Logger
import com.techventus.wikipedianews.view.WikiHeaderViewHolder
import com.techventus.wikipedianews.view.WikiViewHolder

/**
 * Updated WikiAdapter with proper ViewHolder handling and enhanced logging.
 */
class WikiAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mData: ArrayList<WikiData> = ArrayList()

    init {
        Logger.d(TAG, "WikiAdapter initialized")
    }

    fun updateData(items: ArrayList<WikiData>?) {
        Logger.d(TAG, "Updating adapter data with ${items?.size ?: "null"} items")
        mData = items ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = mData[position].type.ordinal
        Logger.v(TAG, "getItemViewType for position $position: $viewType")
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Logger.d(TAG, "onCreateViewHolder called with viewType: $viewType")
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == WikiData.DataType.POST.ordinal) {
            val view = inflater.inflate(R.layout.wiki_item, parent, false)
            Logger.v(TAG, "Inflated wiki_item layout for POST view type")
            WikiViewHolder(parent.context, view)
        } else {
            val view = inflater.inflate(R.layout.wiki_header, parent, false)
            Logger.v(TAG, "Inflated wiki_header layout for HEADER view type")
            WikiHeaderViewHolder(parent.context, view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mData[position]
        Logger.v(TAG, "onBindViewHolder called for position $position with data: ${data.text}")

        try {
            when (getItemViewType(position)) {
                WikiData.DataType.HEADER.ordinal -> {
                    val headerHolder = holder as WikiHeaderViewHolder
                    headerHolder.bindItem(data.text)
                    Logger.v(TAG, "Bound HEADER data at position $position")
                }
                WikiData.DataType.POST.ordinal -> {
                    val postHolder = holder as WikiViewHolder
                    postHolder.bindItem(data.text)
                    Logger.v(TAG, "Bound POST data at position $position")
                }
            }
        } catch (e: ClassCastException) {
            Logger.e(TAG, "ViewHolder casting error at position $position", e)
        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error in onBindViewHolder at position $position", e)
        }
    }

    override fun getItemCount(): Int {
        val count = if (mData.isEmpty()) 0 else mData.size
        Logger.v(TAG, "getItemCount: $count")
        return count
    }

    companion object {
        private val TAG = WikiAdapter::class.java.simpleName
    }
}