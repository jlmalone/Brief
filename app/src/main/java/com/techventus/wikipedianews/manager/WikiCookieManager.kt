package com.techventus.wikipedianews.manager

import android.os.AsyncTask
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.net.CookieManager
import java.net.URI
import java.util.HashMap

/**
 * Created by josephmalone on 15-09-08.
 */
class WikiCookieManager private constructor() : CookieManager() {
    private var mUpdated = false
    private var mAuthTokenInternal: String? = null

    var authToken: String?
        get() = mAuthTokenInternal
        set(authToken) {
            if (authToken == null && mAuthTokenInternal != null || authToken != null && mAuthTokenInternal == null) {
                mUpdated = true
            }
            if (mAuthTokenInternal != null && authToken != null && mAuthTokenInternal != authToken) {
                mUpdated = true
            }
            mAuthTokenInternal = authToken
        }

    /**
     * Set the cookie value in the persistent cookie manager.
     * If the cookie map changes, set the updated flag to true so the PersistenceManager can
     * store the values to shared preferences.
     *
     * @param cookieTuple
     */
    fun setCookie(cookieTuple: Array<String>?) {
        if (cookieTuple != null && cookieTuple.size == 2 && cookieTuple[0] != null && cookieTuple[1] != null && !(COOKIE_MAP.containsKey(cookieTuple[0]) && COOKIE_MAP[cookieTuple[0]] == cookieTuple[1])) {
            mUpdated = true
            setCookie(cookieTuple[0], cookieTuple[1])
        }
    }

    val cookies: Map<String, String>
        get() = COOKIE_MAP

    fun setCookie(key: String, value: String) {
        COOKIE_MAP[key] = value
    }

    private fun saveMap() {
        PreferencesManager.getInstance().setSerialisedCookieMap(COOKIE_MAP, true)
    }

    private fun loadMap() {
        COOKIE_MAP.putAll(PreferencesManager.getInstance().getSerialisedCookieMap())
    }

    fun clearCookies() {
        COOKIE_MAP.clear()
        PreferencesManager.getInstance().clearCookieMap()
        mUpdated = true
        saveCookiesIfNeeded()
    }

    fun saveCookiesIfNeeded() {
        if (mUpdated) {
            object : AsyncTask<Void?, Void?, Void?>() {
                override fun doInBackground(vararg voids: Void?): Void? {
                    synchronized(WikiCookieManager::class.java) {
                        saveMap()
                        mUpdated = false
                    }
                    return null
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    @Throws(IOException::class)
    override fun get(uri: URI, requestHeaders: Map<String, List<String>>): Map<String, List<String>> {
        return super.get(uri, requestHeaders)
    }

    @Throws(IOException::class)
    override fun put(uri: URI, stringListMap: Map<String, List<String>>) {
        super.put(uri, stringListMap)
        if (stringListMap["Set-Cookie"] != null) {
            for (string in stringListMap["Set-Cookie"]!!) {
                setCookie(getCookieKeyValue(string))
            }
        }
        saveCookiesIfNeeded()
    }

    fun listAllOutgoingCookies(): String {
        val cookieMap = cookies
        val sb = StringBuilder()
        for (key in cookieMap.keys) {
            sb.append(key + "=").append(cookieMap[key]).append("; ")
        }
        return sb.toString()
    }

    var cartCount: Int
        get() {
            val cartCountString = COOKIE_MAP[CART_COUNT]
            return if (cartCountString != null && cartCountString.matches("^\\s*\\d*\\s*$".toRegex())) cartCountString.toInt() else 0
        }
        set(cartCount) {
            setCookie(CART_COUNT, cartCount.toString())
        }

    fun clearCartCount() {
        COOKIE_MAP.remove(CART_COUNT)
    }

    fun setUpdated(updated: Boolean) {
        mUpdated = updated
    }

    companion object {
        private const val CART_COUNT = "cartCount"
        private val TAG = WikiCookieManager::class.java.simpleName

        @Volatile
        private var mInstance: WikiCookieManager? = null
        private val COOKIE_MAP: MutableMap<String, String> = HashMap()

        @JvmStatic
        fun getInstance(): WikiCookieManager =
            mInstance ?: synchronized(this) {
                mInstance ?: WikiCookieManager().also {
                    mInstance = it
                    // Load map after instance creation to ensure preferences are available
                    it.loadMap()
                }
            }


        private fun getCookieKeyValue(flatCookieString: String): Array<String>? {
            if (StringUtils.isNotEmpty(flatCookieString) && flatCookieString.contains(";")) {
                val trimmed = flatCookieString.substring(0, flatCookieString.indexOf(";", 0))
                if (trimmed.contains("=")) {
                    return trimmed.split("=".toRegex(), 2).toTypedArray()
                }
            }
            return null
        }
    }
}