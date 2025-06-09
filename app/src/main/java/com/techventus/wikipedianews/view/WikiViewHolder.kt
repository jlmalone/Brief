package com.techventus.wikipedianews.view

import android.content.Context
import android.content.Intent
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.logging.Logger

/**
 * Created by josephmalone on 16-08-23.
 */
class WikiViewHolder(private val mContext: Context, view: View) : WikiHeaderViewHolder(mContext, view) {

    private val mHTMLTextView: TextView? = view.findViewById(R.id.html) // Already in super, but kept for clarity if used directly
    private val mOverflowMenu: ImageView? = view.findViewById(R.id.share)

    init {
        mOverflowMenu?.setImageResource(R.drawable.ic_more_vert_black_24dp)
        Logger.v(TAG, "Overflow menu is null? " + (mOverflowMenu == null))
    }

    // Overriding bindItem to add specific functionality for WikiViewHolder
    override fun bindItem(htmlContent: String?) { // Made htmlContent nullable to match super
        super.bindItem(htmlContent)

        mOverflowMenu?.setOnClickListener { v ->
            val popup = PopupMenu(mContext, v)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.item_overflow_menu, popup.menu)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.itemId == R.id.action_share_item) {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    // Ensure htmlContent is not null before operating on it
                    val noHTMLString = htmlContent?.replace("\\<.*?\\>".toRegex(), "") ?: ""
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "$noHTMLString http://goo.gl/kKFmEp via http://goo.gl/BUemft")
                    sendIntent.putExtra(Intent.EXTRA_HTML_TEXT, "$htmlContent<a href=\"http://goo.gl/kKFmEp\">Wikipedia News</a> via <a href=\"http://goo.gl/BUemft\">Android</a>")
                    sendIntent.type = "text/html"
                    mContext.startActivity(Intent.createChooser(sendIntent, "Share via"))
                    true
                } else {
                    false
                }
            }
            popup.show()
        }
    }
    companion object {
        private val TAG = WikiViewHolder::class.java.simpleName
    }
}