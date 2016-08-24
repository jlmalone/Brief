package com.techventus.wikipedianews.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.logging.Toaster;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiViewHolder extends RecyclerView.ViewHolder
{
	private static final String TAG = WikiViewHolder.class.getSimpleName();
	//	private final ImageView mUserThumbImageView;
	private View mPlaceholder;

	private final TextView mHTMLTextView;

	private final Context mContext;
	Locale locale = new Locale("en", "US");
	NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

	public WikiViewHolder(Context context, View view)
	{
		super(view);
		mContext = context;
//		mUserThumbImageView = (ImageView)view.findViewById(R.id.user_image);
		mHTMLTextView = (TextView)view.findViewById(R.id.html);
	}

	public void bindItem(final String order)
	{
		Logger.v(TAG, "set text "+Html.fromHtml(order));
		mHTMLTextView.setText(Html.fromHtml(order));
				mHTMLTextView.setMovementMethod(LinkMovementMethod.getInstance());

//		setTextViewHTML(mHTMLTextView,order);

	}



	protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
	{
		int start = strBuilder.getSpanStart(span);
		int end = strBuilder.getSpanEnd(span);
		int flags = strBuilder.getSpanFlags(span);
		ClickableSpan clickable = new ClickableSpan() {
			public void onClick(View view) {
				// Do something with span.getURL() to handle the link click...
				Toaster.show(mContext,span.getURL(),Toaster.LENGTH_LONG);
			}
		};
		strBuilder.setSpan(clickable, start, end, flags);
		strBuilder.removeSpan(span);
	}

	protected void setTextViewHTML(TextView text, String html)
	{
		CharSequence sequence = Html.fromHtml(html);
		SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
		URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
		for(URLSpan span : urls) {
			makeLinkClickable(strBuilder, span);
		}
		text.setText(strBuilder);
		text.setMovementMethod(LinkMovementMethod.getInstance());
	}
}
