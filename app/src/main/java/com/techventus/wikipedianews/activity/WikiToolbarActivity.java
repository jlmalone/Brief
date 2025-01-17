package com.techventus.wikipedianews.activity;

import android.os.Bundle;
import androidx.appcompat.widget.SearchView;  // Updated import
import androidx.appcompat.widget.Toolbar;    // Updated import
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
		Logger.d(TAG, "onCreate called");
	}

	@Override
	public void setContentView(int layoutResID)
	{
		super.setContentView(layoutResID);
		checkAndSetToolbar();
		Logger.d(TAG, "setContentView called");
	}

	@Override
	public void setTitle(String title)
	{
		Logger.d(TAG, "Setting title");
		mCurrentTitle = Utils.uppercaseWords(title);
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
			mToolbar = findViewById(R.id.toolbar);  // Modern cast removal
			mToolbarTitle = findViewById(R.id.toolbar_title);
			Logger.d(TAG, "checking for toolbar");

			if (mToolbar != null)
			{
				Logger.d(TAG, "toolbar is not null, setting actionbar");
				setSupportActionBar(mToolbar);
				if (getSupportActionBar() != null) {
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);
					getSupportActionBar().setDisplayShowHomeEnabled(true);
					getSupportActionBar().setDisplayShowTitleEnabled(false);
					getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
				}
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
					setLogoDescription(mCurrentLogoDesc);
				}
			}
		}
	}

	public void hideHomeAsUpIndicator()
	{
		if (getSupportActionBar() != null) {
			getSupportActionBar().setHomeAsUpIndicator(null);
		}
		if (mToolbar != null) {
			mToolbar.setNavigationIcon(null);
		}
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
		// Implement search enabling logic if needed
	}

	public void showUpIndicator(boolean enabled)
	{
		if (getSupportActionBar() != null) {
			getSupportActionBar().setHomeAsUpIndicator(null);
		}
		if (mToolbar != null) {
			mToolbar.setNavigationIcon(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Logger.d(TAG, "onCreateOptionsMenu called");
		mMenu = menu;
		MenuInflater inflater = getMenuInflater();
		// Uncomment and update if you have a search_menu
		// inflater.inflate(R.menu.search_menu, mMenu);
		mToolbarTitle.setVisibility(View.VISIBLE);
		final MenuItem searchItem = menu.findItem(R.id.search);
		// Uncomment and update search logic if needed
        /*
        if (searchItem != null)
        {
            mSearchView = (SearchView) searchItem.getActionView();
            mSearchView.setMaxWidth(Integer.MAX_VALUE);
            if (mSearchView != null)
            {
                mSearchView.setQueryHint(getString(R.string.search_hint));
                final EditText searchTextView = (EditText) mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
                try
                {
                    Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                    mCursorDrawableRes.setAccessible(true);
                    mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor);
                }
                catch (Exception e)
                {
                    // Handle exception appropriately
                }
                searchTextView.setOnKeyListener(new View.OnKeyListener()
                {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event)
                    {
                        if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH && mOnQueryTextListener != null && searchTextView != null)
                        {
                            mOnQueryTextListener.onQueryTextSubmit(searchTextView.getText().toString());
                        }
                        return false;
                    }
                });
                mSearchView.setQuery("", false);
                mSearchView.setSubmitButtonEnabled(false);
                mSearchView.setOnSearchClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (mToolbar != null)
                        {
                            mToolbarTitle.setVisibility(View.GONE);
                            // Implement additional logic if needed
                        }
                    }
                });
                mSearchView.setOnCloseListener(new SearchView.OnCloseListener()
                {
                    @Override
                    public boolean onClose()
                    {
                        closeSearch();
                        // Implement additional logic if needed
                        return false;
                    }
                });
                mSearchView.setOnQueryTextListener(mOnQueryTextListener);
            }
            searchItem.setVisible(mSearchOptionEnabled);
        }
        */
		return super.onCreateOptionsMenu(mMenu);
	}

	SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener()
	{
		@Override
		public boolean onQueryTextSubmit(String s)
		{
			// Implement search submission logic
			// mSearchView.clearFocus();
			mToolbarTitle.setVisibility(View.VISIBLE);
			// startActivity(SearchResultActivity.getStartIntent(WikiToolbarActivity.this, s, originString));
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

	// Uncomment and implement if needed
    /*
    protected void showSearch()
    {
        mSearchView.setIconified(false);
    }

    protected void hideSearch()
    {
        if (mSearchView != null)
        {
            mSearchView.setIconified(true);
        }
    }
    */

	protected void setSearchString(String query)
	{
		// mSearchView.setQuery(query, false);
		// mSearchView.clearFocus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Logger.d(TAG, "onOptionsItemSelected called");
		switch (item.getItemId())
		{
			case android.R.id.home:
				Logger.d(TAG, "Back/Home button pressed");
				// finish();
				break;
//			case R.id.search:
//				Logger.d(TAG, "Search");
//				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}