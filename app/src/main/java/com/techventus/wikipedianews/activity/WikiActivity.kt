package com.techventus.wikipedianews.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.fragment.WikiNewsFragment

/**
 * Created by josephmalone on 16-08-23.
 */
class WikiActivity : WikiToolbarActivity() {

    private var mWikiFragment: WikiNewsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generic_input_fragment_container)

        // Set the title using the Activity's method. This will be handled by WikiToolbarActivity.
        setTitle(R.string.brief)

        // Explicitly ensure the "up" indicator (back arrow) is NOT shown on this main screen.
        showUpIndicator(false)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        if (savedInstanceState == null) {
            mWikiFragment = WikiNewsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container, mWikiFragment!!, WIKI_FRAGMENT_TAG)
                .commit()
        }
    }

    companion object {
        private const val WIKI_FRAGMENT_TAG = "WIKI_FRAGMENT_TAG"
    }
}