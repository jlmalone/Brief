package com.techventus.wikipedianews.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.fragment.WikiFragment;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.util.Utils;

/**
 * Created by josephmalone on 16-06-27.
 * Edited for modern compatibility.
 */
public class WikiToolbarActivity extends BaseActivity implements WikiFragment.ToolbarPropertyCallback {
	private static final String TAG = WikiToolbarActivity.class.getSimpleName();
	protected Toolbar mToolbar;
	private Menu mMenu;
	private TextView mToolbarTitle;
	private String mCurrentTitle = null;
	private String mCurrentNavContentDesc = null;
	private String mCurrentLogoDesc = null;

	// This is declared to support the commented-out search code if you enable it later.
	private SearchView mSearchView;
	private boolean mSearchOptionEnabled; // Assuming this field exists for your search logic

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d(TAG, "onCreate called");
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		checkAndSetToolbar();
		Logger.d(TAG, "setContentView called");
	}

	@Override
	public void setTitle(String title) {
		Logger.d(TAG, "Setting title");
		mCurrentTitle = Utils.uppercaseWords(title);
		if (mToolbar != null && mToolbarTitle != null && title != null) {
			Logger.d(TAG, "Title = " + mCurrentTitle);
			mToolbarTitle.setText(mCurrentTitle);
		}
	}

	public void checkAndSetToolbar() {
		if (mToolbar == null) {
			mToolbar = findViewById(R.id.toolbar);
			mToolbarTitle = findViewById(R.id.toolbar_title);
			Logger.d(TAG, "checking for toolbar");

			if (mToolbar != null) {
				Logger.d(TAG, "toolbar is not null, setting actionbar");
				setSupportActionBar(mToolbar);
				if (getSupportActionBar() != null) {
					// CHANGE 1: This enables the back arrow ('up' indicator).
					getSupportActionBar().setDisplayHomeAsUpEnabled(true);

					// This correctly disables the default title, as you are using a custom TextView.
					getSupportActionBar().setDisplayShowTitleEnabled(false);

					// REMOVED: getSupportActionBar().setDisplayShowHomeEnabled(true);
					// REMOVED: getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
				}
				if (mCurrentTitle != null) {
					mToolbarTitle.setText(mCurrentTitle);
				}
				if (mCurrentNavContentDesc != null) {
					setNavigationContentDescription(mCurrentNavContentDesc);
				}
				if (mCurrentLogoDesc != null) {
					setLogoDescription(mCurrentLogoDesc);
				}
			}
		}
	}

	public void hideHomeAsUpIndicator() {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setHomeAsUpIndicator(null);
		}
		if (mToolbar != null) {
			mToolbar.setNavigationIcon(null);
		}
	}

	@Override
	public void setNavigationContentDescription(String description) {
		mCurrentNavContentDesc = description;
		if (mToolbar != null && description != null) {
			mToolbar.setNavigationContentDescription(description);
		}
	}

	@Override
	public void setLogoDescription(String description) {
		mCurrentLogoDesc = description;
		if (mToolbar != null && description != null) {
			mToolbar.setLogoDescription(description);
		}
	}

	@Override
	public void setSearchEnabled(boolean enabled) {
		// Your original file had this as a stub. It remains a stub.
		// If you re-enable search, you would set mSearchOptionEnabled here.
		mSearchOptionEnabled = enabled;
	}

	/**
	 * CHANGE 2: Fixed this method to correctly show or hide the back arrow.
	 * The original implementation was bugged and always hid the icon.
	 */
	public void showUpIndicator(boolean enabled) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Logger.d(TAG, "onCreateOptionsMenu called");
		mMenu = menu;
		MenuInflater inflater = getMenuInflater();
		// The original commented-out search logic is preserved exactly as it was.
		// To use it, you need to create a menu XML file (e.g., res/menu/search_menu.xml)
		// and uncomment the line below.
		// inflater.inflate(R.menu.search_menu, mMenu);

		if (mToolbarTitle != null) {
			mToolbarTitle.setVisibility(View.VISIBLE);
		}

		final MenuItem searchItem = menu.findItem(R.id.search);
		// All original search code is preserved below inside the comment block.
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

	SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String s) {
			// Implement search submission logic
			// mSearchView.clearFocus();
			if (mToolbarTitle != null) {
				mToolbarTitle.setVisibility(View.VISIBLE);
			}
			// startActivity(SearchResultActivity.getStartIntent(WikiToolbarActivity.this, s, originString));
			return true;
		}

		@Override
		public boolean onQueryTextChange(String s) {
			return false;
		}
	};

	protected void closeSearch() {
		if (mToolbarTitle != null) {
			mToolbarTitle.setVisibility(View.VISIBLE);
		}
	}

	// The original commented-out search logic is preserved exactly as it was.
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

	protected void setSearchString(String query) {
		// mSearchView.setQuery(query, false);
		// mSearchView.clearFocus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.d(TAG, "onOptionsItemSelected called");
		// Using if-else instead of switch for a single case is slightly cleaner.
		if (item.getItemId() == android.R.id.home) {
			// CHANGE 3: The back arrow should trigger backward navigation.
			Logger.d(TAG, "Back/Home button pressed");
			onBackPressed(); // This correctly handles back navigation.
			return true; // We have handled the click, so return true.
		}
		return super.onOptionsItemSelected(item);
	}
}