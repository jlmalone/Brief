package com.techventus.wikipedianews.view

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.logging.Logger

/**
 * Updated WikiHeaderViewHolder with enhanced logging and code cleanup.
 *
 * Created by josephmalone on 16-08-23.
 */
class WikiHeaderViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mHTMLTextView: TextView? = itemView.findViewById(R.id.html)
    // private val mContext: Context // mContext was not used after initialization

    init {
        if (mHTMLTextView == null) {
            Logger.e(TAG, "TextView with ID 'html' not found in the layout.")
        } else {
            Logger.d(TAG, "WikiHeaderViewHolder initialized successfully.")
        }
    }

    /**
     * Binds the HTML text to the TextView.
     *
     * @param htmlContent The HTML content to display.
     */
    fun bindItem(htmlContent: String?) {
        Logger.v(TAG, "Binding header item with HTML content: $htmlContent")

        if (mHTMLTextView != null && htmlContent != null) { // Added null check for htmlContent
            try {
                // Convert HTML to Spanned text and set it to the TextView
                mHTMLTextView.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY)
                mHTMLTextView.movementMethod = LinkMovementMethod.getInstance()
                Logger.v(TAG, "Header content bound successfully.")
            } catch (e: Exception) {
                Logger.e(TAG, "Error binding HTML content to TextView.", e)
            }
        } else {
            if (mHTMLTextView == null) {
                Logger.e(TAG, "Cannot bind item. TextView is null.")
            }
            if (htmlContent == null) {
                Logger.w(TAG, "Cannot bind item. htmlContent is null.")
                mHTMLTextView?.text = "" // Clear text if content is null
            }
        }
    }

    companion object {
        private val TAG = WikiHeaderViewHolder::class.java.simpleName
    }
}
