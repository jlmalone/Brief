package com.techventus.wikipedianews;

import android.app.Application;


//import androidx.multidex.BuildConfig;

import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.logging.Toaster;
import com.techventus.wikipedianews.manager.PreferencesManager;

/**
 * Created by josephmalone on 16-06-23.
 */
public class WikiApplication extends Application
{
	private static final String TAG = WikiApplication.class.getSimpleName();
	private static WikiApplication mWikiApp;

	public static WikiApplication getInstance()
	{
		return mWikiApp;
	}

	@Override
	public void onCreate()
	{
		mWikiApp = this;
		super.onCreate();

		Logger.setShowErrorEnabled(isDebugEnabled());
//		Logger.setEnabled(true);

		if(isDebugEnabled())
		{
			PreferencesManager.getInstance().setLoggingEnabled(true);
		}

		//We have a developer option that enables logging in production.
		//Therefore, we separate the enabling and disabling of it into this
		//if statement.
		if(PreferencesManager.getInstance().isLoggingEnabled())
		{
			Logger.setEnabled(isDebugEnabled());
			Logger.v("BRIEF", "yes this is logging");

		}

		//TODO fix debug flag
		Toaster.setEnabled(isDebugEnabled());
		Toaster.setEnabled(true);

	}

	public static boolean isDebugEnabled()
	{
		return BuildConfig.DEBUG;
	}

}
