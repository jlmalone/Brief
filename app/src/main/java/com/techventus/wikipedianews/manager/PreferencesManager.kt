package com.techventus.wikipedianews.manager

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.techventus.wikipedianews.WikiApplication
import org.apache.commons.lang3.StringUtils
import org.json.JSONException
import org.json.JSONObject

class PreferencesManager private constructor() {
    private val mPreferences: SharedPreferences

    init {
        val context = WikiApplication.getInstance()!!.applicationContext
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getSerialisedCookieMap(): Map<String, String> {
        val jsonString = mPreferences.getString(PREF_COOKIE_MAP, JSONObject().toString())
        val ret: MutableMap<String, String> = HashMap()
        try {
            val jsonObject = JSONObject(jsonString ?: "{}") // Handle null jsonString
            val keysItr = jsonObject.keys()
            while (keysItr.hasNext()) {
                val key = keysItr.next()
                val value = jsonObject[key] as String
                ret[key] = value
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return ret
    }

    fun clearCookieMap() {
        mPreferences.edit().remove(PREF_COOKIE_MAP).apply() // Use apply for asynchronous write
    }

    fun setSerialisedCookieMap(cookieMap: Map<String, String>?, forced: Boolean) {
        if (cookieMap == null) { // Handle null cookieMap
            if (forced) {
                mPreferences.edit().putString(PREF_COOKIE_MAP, JSONObject().toString()).apply()
            }
            return
        }
        val jsonObject = JSONObject(cookieMap)
        val jsonString = jsonObject.toString()
        if (StringUtils.isNotEmpty(jsonString) || forced) { // Simplified condition
            mPreferences.edit().putString(PREF_COOKIE_MAP, jsonString).apply()
        }
    }

    var isLoggingEnabled: Boolean
        get() = mPreferences.getBoolean(PREF_LOGGING_ENABLED, false)
        set(enabled) {
            mPreferences.edit().putBoolean(PREF_LOGGING_ENABLED, enabled).apply()
        }

    // Added new methods for the debug option
    var isDebugModeEnabled: Boolean
        get() = mPreferences.getBoolean(PREF_DEBUG_MODE_ENABLED, false)
        set(enabled) {
            mPreferences.edit().putBoolean(PREF_DEBUG_MODE_ENABLED, enabled).apply()
        }

    companion object {
        private const val PREF_COOKIE_MAP = "PREF_COOKIE_MAP"
        private const val PREF_LOGGING_ENABLED = "PREF_LOGGING_ENABLED"
        private const val PREF_DEBUG_MODE_ENABLED = "PREF_DEBUG_MODE_ENABLED" // Added debug mode preference

        @Volatile
        private var mInstance: PreferencesManager? = null

        @JvmStatic
        fun getInstance(): PreferencesManager =
            mInstance ?: synchronized(this) {
                mInstance ?: PreferencesManager().also { mInstance = it }
            }
    }
}