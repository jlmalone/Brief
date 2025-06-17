package com.techventus.wikipedianews;

import android.app.Application;


//import androidx.multidex.BuildConfig;

import java.util.Calendar;
import java.util.Date;
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

		// Set a default expiration for hardcoded data if none is set (for testing)
		PreferencesManager prefsManager = PreferencesManager.getInstance();
		if (prefsManager.getHardcodedDataExpiration() == 0L) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, 7); // Expires in 7 days
			long defaultExpirationTimestamp = calendar.getTimeInMillis();
			prefsManager.setHardcodedDataExpiration(defaultExpirationTimestamp);
			Logger.i(TAG, "No hardcoded data expiration set. Defaulting to 7 days from now: " + new Date(defaultExpirationTimestamp));
		} else {
			Logger.i(TAG, "Hardcoded data expiration is already set to: " + new Date(prefsManager.getHardcodedDataExpiration()));
		}
	}

	public static boolean isDebugEnabled()
	{
		return BuildConfig.DEBUG;
	}

}
