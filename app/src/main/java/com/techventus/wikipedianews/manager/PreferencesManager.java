package com.techventus.wikipedianews.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.techventus.wikipedianews.WikiApplication;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PreferencesManager
{
	private static final String PREF_COOKIE_MAP = "PREF_COOKIE_MAP";

	private static final String PREF_LOGGING_ENABLED = "PREF_LOGGING_ENABLED";

	private static volatile PreferencesManager mInstance;
	private final SharedPreferences mPreferences;

	public static PreferencesManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (PreferencesManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new PreferencesManager();
				}
			}
		}
		return mInstance;
	}

	private PreferencesManager()
	{
		Context context = WikiApplication.getInstance().getApplicationContext();
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Map<String, String> getSerialisedCookieMap()
	{
		String jsonString = mPreferences.getString(PREF_COOKIE_MAP, (new JSONObject()).toString());
		Map<String, String> ret = new HashMap<>();
		try
		{
			JSONObject jsonObject = new JSONObject(jsonString);
			Iterator<String> keysItr = jsonObject.keys();
			while (keysItr.hasNext())
			{
				String key = keysItr.next();
				String value = (String) jsonObject.get(key);
				ret.put(key, value);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	public void clearCookieMap()
	{
		mPreferences.edit().remove(PREF_COOKIE_MAP).commit();
	}

	public void setSerialisedCookieMap(Map<String, String> cookieMap, boolean forced)
	{
		JSONObject jsonObject = new JSONObject(cookieMap);
		String jsonString = jsonObject.toString();
		if (StringUtils.isNotEmpty(jsonString) || (jsonString != null && forced))
		{
			mPreferences.edit().putString(PREF_COOKIE_MAP, jsonString).commit();
		}
	}

	public boolean isLoggingEnabled()
	{
		return mPreferences.getBoolean(PREF_LOGGING_ENABLED, false);
	}

	public void setLoggingEnabled(boolean enabled)
	{
		mPreferences.edit().putBoolean(PREF_LOGGING_ENABLED, enabled).apply();
	}
}
