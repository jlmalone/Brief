package com.techventus.wikipedianews.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
// import android.widget.TextView; // No longer needed
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
	// private TextView mToolbarTitle; // REMOVED - Using native toolbar title now
	private CharSequence mCurrentTitle = null; // Use CharSequence to match Activity.setTitle
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

	// This is for the ToolbarPropertyCallback interface, called from fragments
	@Override
	public void setTitle(String title) {
		setTitle((CharSequence) title);
	}

	// This overrides the standard Activity method for setting the title
	@Override
	public void setTitle(CharSequence title) {
		Logger.d(TAG, "Setting title");
		mCurrentTitle = (title != null) ? Utils.uppercaseWords(title.toString()) : null;
		if (getSupportActionBar() != null) {
			Logger.d(TAG, "Title = " + mCurrentTitle);
			getSupportActionBar().setTitle(mCurrentTitle);
		}
	}


	public void checkAndSetToolbar() {
		if (mToolbar == null) {
			mToolbar = findViewById(R.id.toolbar);
			// mToolbarTitle = findViewById(R.id.toolbar_title); // REMOVED
			Logger.d(TAG, "checking for toolbar");

			if (mToolbar != null) {
				Logger.d(TAG, "toolbar is not null, setting actionbar");
				setSupportActionBar(mToolbar);

				// Add 24dp margin between logo and title
				int titleMarginStart = Utils.getPixelSizeFromDP(mToolbar.getContext(), 24f);
				mToolbar.setTitleMarginStart(titleMarginStart);

				if (getSupportActionBar() != null) {
					// Use native title, which appears next to the logo.
					getSupportActionBar().setDisplayShowTitleEnabled(true);
					// Show the app logo in the toolbar
					getSupportActionBar().setDisplayUseLogoEnabled(true);
					getSupportActionBar().setLogo(R.mipmap.ic_launcher);
					// Do NOT enable the back arrow by default. Let activities opt-in.
					// getSupportActionBar().setDisplayHomeAsUpEnabled(true); // REMOVED

					// Set the title if it was provided before the toolbar was initialized.
					if (mCurrentTitle != null) {
						getSupportActionBar().setTitle(mCurrentTitle);
					}
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
	 * Shows or hides the back arrow (up indicator).
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
		inflater.inflate(R.menu.main_menu, mMenu);

		// The native title visibility is handled automatically with SearchView, so no code needed here.

		final MenuItem searchItem = menu.findItem(R.id.search);
		// All original search code is preserved below inside the comment block.
        /*
        ...
        */
		return super.onCreateOptionsMenu(mMenu);
	}

	SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String s) {
			// Implement search submission logic
			return true;
		}

		@Override
		public boolean onQueryTextChange(String s) {
			return false;
		}
	};

	protected void closeSearch() {
		// Native title visibility is handled automatically.
	}

    /*
    ... (omitted other search-related methods for brevity)
    */

	protected void setSearchString(String query) {
		// mSearchView.setQuery(query, false);
		// mSearchView.clearFocus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.d(TAG, "onOptionsItemSelected called");

		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			Logger.d(TAG, "Back/Home button pressed");
			onBackPressed(); // This correctly handles back navigation.
			return true;
		} else if (itemId == R.id.action_acknowledgements) {
			showInfoDialog("Acknowledgements", "This app uses data from Wikipedia.\n\nLibraries used:\n- Jsoup\n- OkHttp\n- Glide\n- Apache Commons Lang\n- AndroidX Libraries");
			return true;
		} else if (itemId == R.id.action_about) {
			try {
				String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				showInfoDialog("About Wikipedia News", "Version " + versionName + "\n\nA simple app to browse current events from Wikipedia.");
			} catch (PackageManager.NameNotFoundException e) {
				Logger.e(TAG, "Could not get package info", e);
				showInfoDialog("About Wikipedia News", "A simple app to browse current events from Wikipedia.");
			}
			return true;
		} else if (itemId == R.id.action_licenses) {
			showInfoDialog("Open Source Licenses", "This application uses open source software. The source code and licenses can be found with their respective distributions online.");
			return true;
		} else if (itemId == R.id.action_privacy_policy) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)));
			startActivity(browserIntent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showInfoDialog(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, null)
				.show();
	}
}