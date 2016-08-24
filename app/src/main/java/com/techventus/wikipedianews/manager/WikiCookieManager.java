package com.techventus.wikipedianews.manager;

/**
 * Created by josephmalone on 16-06-23.
 */

import android.os.AsyncTask;
import com.techventus.wikipedianews.util.ArrayUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by josephmalone on 15-09-08.
 */
public class WikiCookieManager extends CookieManager
{
	private static final String CART_COUNT = "cartCount";

	private static final String TAG = WikiCookieManager.class.getSimpleName();

	private boolean mUpdated;

	private static volatile WikiCookieManager mInstance;

	private static Map<String, String> COOKIE_MAP;

	private String mAuthToken;

	public String getAuthToken()
	{
		return mAuthToken;
	}

	public void setAuthToken(final String authToken)
	{
		if ((authToken == null && mAuthToken != null) || (authToken != null && mAuthToken == null))
		{
			mUpdated = true;
		}
		if (mAuthToken != null && authToken != null && !mAuthToken.equals(authToken))
		{
			mUpdated = true;
		}
		mAuthToken = authToken;
	}

	/**
	 * Set the cookie value in the persistent cookie manager.
	 * If the cookie map changes, set the updated flag to true so the PersistenceManager can
	 * store the values to shared preferences.
	 *
	 * @param cookieTuple
	 * @return
	 */
	public void setCookie(final String[] cookieTuple)
	{
		if (!ArrayUtil.isNullOrContainsEmpty(cookieTuple) && cookieTuple.length == 2 && !(COOKIE_MAP.containsKey(cookieTuple[0]) && COOKIE_MAP.get(
				cookieTuple[0]).equals(cookieTuple[1])))
		{
			mUpdated = true;
			setCookie(cookieTuple[0], cookieTuple[1]);
		}
	}

	public Map<String, String> getCookies()
	{
		return COOKIE_MAP;
	}

	public void setCookie(final String key, final String value)
	{
		COOKIE_MAP.put(key, value);
	}

	public static WikiCookieManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (WikiCookieManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new WikiCookieManager();
				}
			}
		}

		return mInstance;
	}

	private WikiCookieManager()
	{
		super();

		if (COOKIE_MAP == null)
		{
			COOKIE_MAP = new HashMap<>();
		}
		loadMap();
	}

	private void saveMap()
	{
		PreferencesManager.getInstance().setSerialisedCookieMap(COOKIE_MAP, true);
	}

	private void loadMap()
	{
		COOKIE_MAP.putAll(PreferencesManager.getInstance().getSerialisedCookieMap());
	}

	public void clearCookies()
	{
		COOKIE_MAP.clear();
		PreferencesManager.getInstance().clearCookieMap();
		mUpdated = true;
		saveCookiesIfNeeded();

	}

	public void saveCookiesIfNeeded()
	{
		if (mUpdated)
		{
			new AsyncTask<Void, Void, Void>()
			{
				@Override
				protected Void doInBackground(Void... voids)
				{
					synchronized (WikiCookieManager.class)
					{
						saveMap();
						mUpdated = false;
					}
					return null;
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}


	private static String[] getCookieKeyValue(final String flatCookieString)
	{
		if (StringUtils.isNotEmpty(flatCookieString) && flatCookieString.contains(";"))
		{
			String trimmed = flatCookieString.substring(0, flatCookieString.indexOf(";", 0));
			if (trimmed.contains("="))
			{
				return trimmed.split("=", 2);
			}
		}

		return null;
	}

	@Override
	public Map<String, List<String>> get(final URI uri, final Map<String, List<String>> requestHeaders) throws IOException
	{
		Map<String, List<String>> ret = super.get(uri, requestHeaders);
		return ret;
	}

	@Override
	public void put(final URI uri, final Map<String, List<String>> stringListMap) throws IOException
	{
		super.put(uri, stringListMap);

		if (stringListMap != null && stringListMap.get("Set-Cookie") != null)
		{
			for (String string : stringListMap.get("Set-Cookie"))
			{
				setCookie(getCookieKeyValue(string));
			}
		}
		saveCookiesIfNeeded();
	}

	public String listAllOutgoingCookies()
	{
		Map<String, String> cookieMap = getCookies();
		StringBuilder sb = new StringBuilder();

		for (String key : cookieMap.keySet())
		{
			sb.append(key + "=").append(cookieMap.get(key)).append("; ");
		}
		return sb.toString();
	}

	public int getCartCount()
	{
		String cartCountString = COOKIE_MAP.get(CART_COUNT);
		return cartCountString != null && cartCountString.matches("^\\s*\\d*\\s*$") ? Integer.parseInt(cartCountString) : 0;
	}

	public void setCartCount(int cartCount)
	{
		setCookie(CART_COUNT, String.valueOf(cartCount));
	}

	public void clearCartCount()
	{
		COOKIE_MAP.remove(CART_COUNT);
	}

	public void setUpdated(boolean updated)
	{
		mUpdated = updated;
	}
}
