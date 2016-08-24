package com.techventus.wikipedianews.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.fragment.WikiFragment;
import com.techventus.wikipedianews.fragment.WikiNewsFragment;

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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (savedInstanceState == null)
		{
			Intent intent = getIntent();
			mWikiFragment = new WikiNewsFragment();
			getFragmentManager().beginTransaction().add(R.id.container, mWikiFragment, WIKI_FRAGMENT_TAG).commit();
		}
	}

}
