package com.techventus.wikipedianews.view;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView; // Changed import
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.techventus.wikipedianews.R;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiHeaderViewHolder extends RecyclerView.ViewHolder
{
	//	private final ImageView mUserThumbImageView;
	private View mPlaceholder;

	private final TextView mHTMLTextView;

	private final Context mContext;
	Locale locale = new Locale("en", "US");
	NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

	public WikiHeaderViewHolder(Context context, View view)
	{
		super(view);
		mContext = context;
//		mUserThumbImageView = (ImageView)view.findViewById(R.id.user_image);
		mHTMLTextView = (TextView)view.findViewById(R.id.html);
	}

	public void bindItem(final String order)
	{
//		mHTMLTextView.superetText(Html.fromHtml(order));

//		Logger.v(TAG, "setTextt text " + Html.fromHtml(order));
		mHTMLTextView.setText(Html.fromHtml(order));
		mHTMLTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}
}