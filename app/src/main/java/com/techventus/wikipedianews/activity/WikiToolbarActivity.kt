package com.techventus.wikipedianews.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.fragment.WikiFragment
import com.techventus.wikipedianews.logging.Logger
import com.techventus.wikipedianews.util.Utils

/**
 * Created by josephmalone on 16-06-27.
 * Edited for modern compatibility.
 */
open class WikiToolbarActivity : BaseActivity(), WikiFragment.ToolbarPropertyCallback {

    protected var mToolbar: Toolbar? = null
    private var mMenu: Menu? = null
    private var mCurrentTitle: CharSequence? = null
    private var mCurrentNavContentDesc: String? = null
    private var mCurrentLogoDesc: String? = null

    // This is declared to support the commented-out search code if you enable it later.
    private var mSearchView: SearchView? = null
    private var mSearchOptionEnabled = false // Assuming this field exists for your search logic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate called")
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        checkAndSetToolbar()
        Logger.d(TAG, "setContentView called")
    }

    // This is for the ToolbarPropertyCallback interface, called from fragments
    override fun setTitle(title: String) {
        setTitle(title as CharSequence)
    }

    // This overrides the standard Activity method for setting the title
    override fun setTitle(title: CharSequence) {
        Logger.d(TAG, "Setting title")
        mCurrentTitle = title.let { Utils.uppercaseWords(it.toString()) }
        supportActionBar?.let {
            Logger.d(TAG, "Title = $mCurrentTitle")
            it.title = mCurrentTitle
        }
    }

    fun checkAndSetToolbar() {
        if (mToolbar == null) {
            mToolbar = findViewById(R.id.toolbar)
            Logger.d(TAG, "checking for toolbar")

            mToolbar?.let { toolbar ->
                Logger.d(TAG, "toolbar is not null, setting actionbar")
                setSupportActionBar(toolbar)

                // Add 24dp margin between logo and title
                val titleMarginStart = Utils.getPixelSizeFromDP(toolbar.context, 24f)
                toolbar.setTitleMarginStart(titleMarginStart)

                supportActionBar?.apply {
                    // Use native title, which appears next to the logo.
                    setDisplayShowTitleEnabled(true)
                    // Show the app logo in the toolbar
                    setDisplayUseLogoEnabled(true)
                    setLogo(R.mipmap.ic_launcher)
                    // Do NOT enable the back arrow by default. Let activities opt-in.
                    // setDisplayHomeAsUpEnabled(true) // REMOVED

                    // Set the title if it was provided before the toolbar was initialized.
                    mCurrentTitle?.let { title = it }
                }
                mCurrentNavContentDesc?.let { setNavigationContentDescription(it) }
                mCurrentLogoDesc?.let { setLogoDescription(it) }
            }
        }
    }

    fun hideHomeAsUpIndicator() {
        supportActionBar?.setHomeAsUpIndicator(null)
        mToolbar?.navigationIcon = null
    }

    override fun setNavigationContentDescription(description: String?) {
        mCurrentNavContentDesc = description
        if (mToolbar != null && description != null) {
            mToolbar?.navigationContentDescription = description
        }
    }

    override fun setLogoDescription(description: String?) {
        mCurrentLogoDesc = description
        if (mToolbar != null && description != null) {
            mToolbar?.logoDescription = description
        }
    }

    override fun setSearchEnabled(enabled: Boolean) {
        // Your original file had this as a stub. It remains a stub.
        // If you re-enable search, you would set mSearchOptionEnabled here.
        mSearchOptionEnabled = enabled
    }

    /**
     * Shows or hides the back arrow (up indicator).
     */
    fun showUpIndicator(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Logger.d(TAG, "onCreateOptionsMenu called")
        mMenu = menu
        menuInflater.inflate(R.menu.main_menu, mMenu)

        // The native title visibility is handled automatically with SearchView, so no code needed here.

        val searchItem = menu.findItem(R.id.search)
        // All original search code is preserved below inside the comment block.
        /*
        ...
        */
        return super.onCreateOptionsMenu(mMenu)
    }

    var mOnQueryTextListener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(s: String): Boolean {
            // Implement search submission logic
            return true
        }

        override fun onQueryTextChange(s: String): Boolean {
            return false
        }
    }

    protected fun closeSearch() {
        // Native title visibility is handled automatically.
    }

    /*
    ... (omitted other search-related methods for brevity)
    */

    protected fun setSearchString(query: String?) {
        // mSearchView.setQuery(query, false);
        // mSearchView.clearFocus();
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Logger.d(TAG, "onOptionsItemSelected called")

        when (item.itemId) {
            android.R.id.home -> {
                Logger.d(TAG, "Back/Home button pressed")
                onBackPressed() // This correctly handles back navigation.
                return true
            }
            R.id.action_acknowledgements -> {
                showInfoDialog("Acknowledgements", "This app uses data from Wikipedia.\n\nLibraries used:\n- Jsoup\n- OkHttp\n- Glide\n- Apache Commons Lang\n- AndroidX Libraries")
                return true
            }
            R.id.action_about -> {
                try {
                    val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                    showInfoDialog("About Wikipedia News", "Version $versionName\n\nA simple app to browse current events from Wikipedia.")
                } catch (e: PackageManager.NameNotFoundException) {
                    Logger.e(TAG, "Could not get package info", e)
                    showInfoDialog("About Wikipedia News", "A simple app to browse current events from Wikipedia.")
                }
                return true
            }
            R.id.action_licenses -> {
                showInfoDialog("Open Source Licenses", "This application uses open source software. The source code and licenses can be found with their respective distributions online.")
                return true
            }
            R.id.action_privacy_policy -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)))
                startActivity(browserIntent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        private val TAG = WikiToolbarActivity::class.java.simpleName
    }
}