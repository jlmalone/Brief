package com.techventus.wikipedianews.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.logging.Logger;

import java.util.Locale;

/**
 * Updated WikiHeaderViewHolder with enhanced logging and code cleanup.
 *
 * Created by josephmalone on 16-08-23.
 */
public class WikiHeaderViewHolder extends RecyclerView.ViewHolder {
	private static final String TAG = WikiHeaderViewHolder.class.getSimpleName();

	private final TextView mHTMLTextView;
	private final Context mContext;

	public WikiHeaderViewHolder(Context context, View itemView) {
		super(itemView);
		mContext = context;
		mHTMLTextView = itemView.findViewById(R.id.html);

		if (mHTMLTextView == null) {
			Logger.e(TAG, "TextView with ID 'html' not found in the layout.");
		} else {
			Logger.d(TAG, "WikiHeaderViewHolder initialized successfully.");
		}
	}

	/**
	 * Binds the HTML text to the TextView.
	 *
	 * @param htmlContent The HTML content to display.
	 */
	public void bindItem(final String htmlContent) {
		Logger.v(TAG, "Binding header item with HTML content: " + htmlContent);

		if (mHTMLTextView != null) {
			try {
				// Convert HTML to Spanned text and set it to the TextView
				mHTMLTextView.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));
				mHTMLTextView.setMovementMethod(LinkMovementMethod.getInstance());
				Logger.v(TAG, "Header content bound successfully.");
			} catch (Exception e) {
				Logger.e(TAG, "Error binding HTML content to TextView.", e);
			}
		} else {
			Logger.e(TAG, "Cannot bind item. TextView is null.");
		}
	}
}
