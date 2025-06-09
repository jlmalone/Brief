package com.techventus.wikipedianews.logging

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by josephmalone on 6/15/15.
 */
object Logger {
    // Prefix to help filtering of logcat for output from our app
    private const val PREFIX = "#WIKI# "
    private const val AT = "@"
    private const val PARENTHETIC_NULL = "(null)"

    // ***Don't change this*** it's now set on app startup based on the debuggable flag in the manifest
    @JvmField
    var showlog = false

    @JvmField
    var showErrorLog = false

    /**
     * Return whether logging is enabled (implies the debuggable flag is set)
     *
     * @return true if logging is enabled
     */
    @JvmStatic
    fun getShowlog(): Boolean {
        return showlog
    }

    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        showlog = enabled
        i("Logger", "Logger ENABLED.")
    }

    @JvmStatic
    fun setShowErrorEnabled(enabled: Boolean) {
        showErrorLog = enabled
    }

    @JvmStatic
    fun e(tag: String, message: String?, e: Throwable?) {
        var tag = tag
        if (showlog || showErrorLog) {
            tag += AT + Thread.currentThread().name
            Log.e(tag, PREFIX + (message ?: PARENTHETIC_NULL), e)
        }
    }

    @JvmStatic
    fun e(tag: String, message: String?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            Log.e(tag, PREFIX + (message ?: PARENTHETIC_NULL))
        }
    }

    @JvmStatic
    fun v(tag: String, message: String?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            Log.v(tag, PREFIX + (message ?: PARENTHETIC_NULL))
        }
    }

    @JvmStatic
    fun w(tag: String, message: String?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            Log.w(tag, PREFIX + (message ?: PARENTHETIC_NULL))
        }
    }

    @JvmStatic
    fun i(tag: String, message: String?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            Log.i(tag, PREFIX + (message ?: PARENTHETIC_NULL))
        }
    }

    @JvmStatic
    fun d(tag: String, message: String?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            Log.d(tag, PREFIX + (message ?: PARENTHETIC_NULL))
        }
    }

    @JvmStatic
    fun logExtras(tag: String, context: Context, src: Intent?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            if (src?.extras == null) {
                return
            }
            val keys = src.extras!!.keySet()
            for (key in keys) {
                d(tag, String.format("$context, intent extra %s=%s", key, src.extras!![key].toString()))
            }
        }
    }

    /**
     * Logs a stack trace to TTY.
     *
     * @param tag              Debug tag to display in log output
     * @param e                Throwable to display
     */
    @JvmStatic
    fun logStackTrace(tag: String, e: Throwable?) {
        var tag = tag
        if (showlog) {
            tag += AT + Thread.currentThread().name
            // Legend tells of NULL exceptions roaming the world of Java...
            if (e != null) {
                // Display exception type.
                d(tag, e.toString())

                // Display stack trace lines.
                for (elem in e.stackTrace) {
                    // Copies the format of Exception.printStackTrace().
                    d(tag, "at $elem")
                }
            } else {
                d(tag, "NULL exception passed to logStackTrace(). This should never happen!")
            }
        }
    }
}