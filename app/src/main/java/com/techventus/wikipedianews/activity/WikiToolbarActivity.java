package com.techventus.wikipedianews.activity;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.fragment.WikiFragment;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.util.Utils;

/**
 * Created by josephmalone on 16-06-27.
 */
public class WikiToolbarActivity extends BaseActivity implements WikiFragment.ToolbarPropertyCallback
{
	private static final String TAG = WikiToolbarActivity.class.getSimpleName();
	protected Toolbar mToolbar;
	private Menu mMenu;
	private TextView mToolbarTitle;
	private String mCurrentTitle = null;
	private String mCurrentNavContentDesc = null;
	private String mCurrentLogoDesc = null;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(layoutResID);
		checkAndSetToolbar();
	}

	@Override
	public void setTitle(String title)
	{
		Logger.d(TAG, "Setting title");
		mCurrentTitle =  Utils.uppercaseWords(title);
		if (mToolbar != null && mToolbarTitle != null && title != null)
		{
			Logger.d(TAG, "Title = " + mCurrentTitle);
			mToolbarTitle.setText(mCurrentTitle);
		}
	}

	public void checkAndSetToolbar()
	{
		if (mToolbar == null)
		{
			mToolbar = (Toolbar) findViewById(R.id.toolbar);
			mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
			if (mToolbar != null)
			{
				this.setSupportActionBar(mToolbar);
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setDisplayShowHomeEnabled(true);
				getSupportActionBar().setDisplayShowTitleEnabled(false);
				getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
				if (mCurrentTitle != null)
				{
					mToolbarTitle.setText(mCurrentTitle);
				}
				if (mCurrentNavContentDesc != null)
				{
					setNavigationContentDescription(mCurrentNavContentDesc);
				}
				if (mCurrentLogoDesc != null)
				{
					setNavigationContentDescription(mCurrentLogoDesc);
				}
			}
		}
	}

	public void hideHomeAsUpIndicator()
	{
		getSupportActionBar().setHomeAsUpIndicator(null);
	}

	@Override
	public void setNavigationContentDescription(String description)
	{
		mCurrentNavContentDesc = description;
		if (mToolbar != null && description != null)
		{
			mToolbar.setNavigationContentDescription(description);
		}
	}

	@Override
	public void setLogoDescription(String description)
	{
		mCurrentLogoDesc = description;
		if (mToolbar != null && description != null)
		{
			mToolbar.setLogoDescription(description);
		}
	}

	@Override
	public void setSearchEnabled(boolean enabled)
	{

	}


	public void showUpIndicator(boolean enabled)
	{
		getSupportActionBar().setHomeAsUpIndicator(null);
		mToolbar.setNavigationIcon(null);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		mMenu = menu;
		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.search_menu, mMenu);
		mToolbarTitle.setVisibility(View.VISIBLE);
		final MenuItem searchItem = menu.findItem(R.id.search);
//		if (searchItem != null)
//		{
//			mSearchView = (SearchView) searchItem.getActionView();
//			mSearchView.setMaxWidth(Integer.MAX_VALUE);
//			if (mSearchView != null)
//			{
//				mSearchView.setQueryHint(getString(R.string.search_hint));
//				final EditText searchTextView = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//				try
//				{
//					Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
//					mCursorDrawableRes.setAccessible(true);
//					mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor);
//				}
//				catch (Exception e)
//				{
//					//TODO consider removing or replacing with another analytic since
//					//this may happen all the time on certain devices
//				}
//				searchTextView.setOnKeyListener(new View.OnKeyListener()
//				{
//					@Override
//					public boolean onKey(View v, int keyCode, KeyEvent event)
//					{
//						if (event.getKeyCode() == 84 && mOnQueryTextListener != null && searchTextView != null)
//						{
//							mOnQueryTextListener.onQueryTextSubmit(searchTextView.getText().toString());
//						}
//						return false;
//					}
//				});
//				mSearchView.setQuery("", false);
//				mSearchView.setSubmitButtonEnabled(false);
//				mSearchView.setOnSearchClickListener(new View.OnClickListener()
//				{
//					@Override
//					public void onClick(View v)
//					{
//						if (mToolbar != null)
//						{
//							mToolbarTitle.setVisibility(View.GONE);
//							//TODO TEMP
//							setCartEnabled(false);
//						}
//					}
//				});
//				mSearchView.setOnCloseListener(new SearchView.OnCloseListener()
//				{
//					@Override
//					public boolean onClose()
//					{
//						closeSearch();
//						setCartEnabled(true);
//
//						return false;
//					}
//				});
//				mSearchView.setOnQueryTextListener(mOnQueryTextListener);
//			}
//			searchItem.setVisible(mSearchOptionEnabled);
//		}



		return super.onCreateOptionsMenu(mMenu);
	}

	SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener()
	{
		@Override
		public boolean onQueryTextSubmit(String s)
		{
			//TODO reenable
//			mSearchView.clearFocus();
			mToolbarTitle.setVisibility(View.VISIBLE);
			//TODO start search results activity
//			startActivity(SearchResultActivity.getStartIntent(WikiToolbarActivity.this, s, originString));
			return true;
		}

		@Override
		public boolean onQueryTextChange(String s)
		{
			return false;
		}
	};



	protected void closeSearch()
	{
		if (mToolbar != null)
		{
			mToolbarTitle.setVisibility(View.VISIBLE);
		}
	}

//	protected void showSearch()
//	{
//		mSearchView.setIconified(false);
//	}
//
//	protected void hideSearch()
//	{
//		if (mSearchView != null)
//		{
//			mSearchView.setIconified(true);
//		}
//	}

	protected void setSearchString(String query)
	{
//		mSearchView.setQuery(query, false);
//		mSearchView.clearFocus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Logger.d(TAG, "Back/Home button pressed");
//				finish();
				break;
			case R.id.search:
				Logger.d(TAG, "Search");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
