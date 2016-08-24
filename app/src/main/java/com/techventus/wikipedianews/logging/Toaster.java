package com.techventus.wikipedianews.logging;

import android.content.Context;
import android.widget.Toast;
import com.techventus.wikipedianews.WikiApplication;


/**
 * Created by josephmalone on 15-09-24.
 */
public class Toaster
{

	public static final int LENGTH_LONG = Toast.LENGTH_LONG;
	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

	// Prefix to help filtering of logcat for output from our app
	private static final String PREFIX = "#DEBUG# ";
	// ***Don't change this*** it's now set on app startup based on the debuggable flag in the manifest
	private static boolean showToast;

	/**
	 * Return whether logging is enabled (implies the debuggable flag is set)
	 *
	 * @return true if logging is enabled
	 */
	public static boolean getShowlog()
	{
		return showToast;
	}

	public static void setEnabled(Boolean enabled)
	{
		showToast = enabled;
		Logger.i("Logger", "Toaster ENABLED.");
	}


	public static void show(int stringId)
	{
		if (showToast)
		{
			Toast.makeText(WikiApplication.getInstance(), stringId, Toast.LENGTH_LONG).show();
		}
	}

	public static void show(Context context, int StringId, int lengthInMillis)
	{
		if (showToast && context != null)
		{
			Toast.makeText(context, StringId, lengthInMillis).show();
		}
	}

	public static void show(Context context, String string, int lengthInMillis)
	{
		if (showToast && context != null)
		{
			Toast.makeText(context, string, lengthInMillis).show();
		}
	}
}
