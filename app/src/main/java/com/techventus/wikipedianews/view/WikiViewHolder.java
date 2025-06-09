package com.techventus.wikipedianews.view;

import android.content.Context;
import android.content.Intent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.logging.Logger;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiViewHolder extends WikiHeaderViewHolder
{
	private static final String TAG = WikiViewHolder.class.getSimpleName();

	private final TextView mHTMLTextView;
	private final ImageView mOverflowMenu;

	private final Context mContext;

	public WikiViewHolder(Context context, View view)
	{
		super(context, view);
		mContext = context;
		mHTMLTextView = (TextView) view.findViewById(R.id.html);
		mOverflowMenu = (ImageView) view.findViewById(R.id.share);
		if (mOverflowMenu != null) {
			mOverflowMenu.setImageResource(R.drawable.ic_more_vert_black_24dp);
		}
		Logger.v(TAG, "Overflow menu is null? "+(mOverflowMenu==null));
	}

	public void bindItem(final String order)
	{
		super.bindItem(order);

		if(mOverflowMenu!=null)
		{
			mOverflowMenu.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					PopupMenu popup = new PopupMenu(mContext, v);
					MenuInflater inflater = popup.getMenuInflater();
					inflater.inflate(R.menu.item_overflow_menu, popup.getMenu());
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							if (item.getItemId() == R.id.action_share_item) {
								Intent sendIntent = new Intent();
								sendIntent.setAction(Intent.ACTION_SEND);
								String noHTMLString = order.replaceAll("\\<.*?\\>", "");
								sendIntent.putExtra(Intent.EXTRA_TEXT, noHTMLString + " http://goo.gl/kKFmEp via http://goo.gl/BUemft");
								sendIntent.putExtra(Intent.EXTRA_HTML_TEXT, order + "<a href=\"http://goo.gl/kKFmEp\">Wikipedia News</a> via <a href=\"http://goo.gl/BUemft\">Android</a>");
								sendIntent.setType("text/html");
								mContext.startActivity(Intent.createChooser(sendIntent, "Share via"));
								return true;
							}
							return false;
						}
					});
					popup.show();
				}
			});
		}
	}
}