package com.techventus.wikipedianews.logging

import android.content.Context
import android.widget.Toast
import com.techventus.wikipedianews.App

/**
 * Created by josephmalone on 15-09-24.
 */
object Toaster {
    const val LENGTH_LONG = Toast.LENGTH_LONG
    const val LENGTH_SHORT = Toast.LENGTH_SHORT

    // Prefix to help filtering of logcat for output from our app
    private const val PREFIX = "#DEBUG# "

    // ***Don't change this*** it's now set on app startup based on the debuggable flag in the manifest
    @JvmField
    var showToast = false

    /**
     * Return whether logging is enabled (implies the debuggable flag is set)
     *
     * @return true if logging is enabled
     */
    @JvmStatic
    fun getShowlog(): Boolean {
        return showToast
    }

    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        showToast = enabled
        Logger.i("Logger", "Toaster ENABLED.")
    }

    @JvmStatic
    fun show(stringId: Int) {
        if (showToast) {
            App.getInstance()?.let {
                Toast.makeText(it, stringId, Toast.LENGTH_LONG).show()
            }
        }
    }

    @JvmStatic
    fun show(context: Context?, stringId: Int, lengthInMillis: Int) {
        if (showToast && context != null) {
            Toast.makeText(context, stringId, lengthInMillis).show()
        }
    }

    @JvmStatic
    fun show(context: Context?, string: String?, lengthInMillis: Int) {
        if (showToast && context != null && string != null) {
            Toast.makeText(context, string, lengthInMillis).show()
        }
    }
}
