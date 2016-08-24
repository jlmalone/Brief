package com.techventus.wikipedianews.logging;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

/**
 * Created by josephmalone on 6/15/15.
 */
public class Logger
{
	// Prefix to help filtering of logcat for output from our app
	private static final String PREFIX = "#WIKI# ";
	private static final String AT = "@";
	private static final String PARENTHETIC_NULL = "(null)";
	// ***Don't change this*** it's now set on app startup based on the debuggable flag in the manifest
	private static boolean showlog = false;
	private static boolean showErrorLog = false;

	/**
	 * Return whether logging is enabled (implies the debuggable flag is set)
	 *
	 * @return true if logging is enabled
	 */
	public static Boolean getShowlog()
	{
		return showlog;
	}

	public static void setEnabled(Boolean enabled)
	{
		showlog = enabled;
		i("Logger", "Logger ENABLED.");
	}

	public static void setShowErrorEnabled(boolean enabled){
		showErrorLog = enabled;
	}

	public static void e(String TAG, String message, Throwable e)
	{
		if (showlog || showErrorLog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.e(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message), e);
		}
	}

	public static void e(String TAG, String message)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.e(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message));
		}
	}

	public static void v(String TAG, String message)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.v(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message));
		}
	}

	public static void w(String TAG, String message)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.w(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message));
		}
	}

	public static void i(String TAG, String message)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.i(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message));
		}
	}

	public static void d(String TAG, String message)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			Log.d(TAG, PREFIX + ((message == null) ? PARENTHETIC_NULL : message));
		}
	}

	public static void logExtras(String TAG, Context context, Intent src)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			if (src == null || src.getExtras() == null)
			{
				return;
			}
			Set<String> keys = src.getExtras().keySet();
			for (String key : keys)
			{
				d(TAG, String.format(context + ", intent extra %s=%s", key, src.getExtras().get(key).toString()));
			}
		}
	}


	/**
	 * Logs a stack trace to TTY.
	 *
	 * @param TAG              Debug tag to display in log output
	 * @param e                Throwable to display
	 */
	public static void logStackTrace(String TAG, Throwable e)
	{
		if (showlog)
		{
			TAG += AT + Thread.currentThread().getName();
			// Legend tells of NULL exceptions roaming the world of Java...
			if (e != null)
			{
				// Display exception type.
				d(TAG, e.toString());

				// Display stack trace lines.
				for (StackTraceElement elem : e.getStackTrace())
				{
					// Copies the format of Exception.printStackTrace().
					d(TAG, "at " + elem.toString());
				}
			}
			else
			{
				d(TAG, "NULL exception passed to logStackTrace(). This should never happen!");
			}
		}
	}

}