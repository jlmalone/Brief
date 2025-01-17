package com.techventus.wikipedianews.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;  // Updated import
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.fragment.WikiNewsFragment;  // Ensure this fragment extends androidx.fragment.app.Fragment

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiActivity extends WikiToolbarActivity
{
	private static final String WIKI_FRAGMENT_TAG = "WIKI_FRAGMENT_TAG";
	private WikiNewsFragment mWikiFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generic_input_fragment_container);
		Toolbar toolbar = findViewById(R.id.toolbar);  // Modern cast removal
		if (savedInstanceState == null)
		{
			Intent intent = getIntent();
			mWikiFragment = new WikiNewsFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, mWikiFragment, WIKI_FRAGMENT_TAG)
					.commit();
		}
	}
}
