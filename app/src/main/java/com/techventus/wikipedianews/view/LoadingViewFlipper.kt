package com.techventus.wikipedianews.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.Nullable
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.logging.Logger

/**
 * Custom ViewFlipper to manage loading, content, and error views with retry functionality.
 */
class LoadingViewFlipper : ViewFlipper {
    // Child Views
    private var mLoadingView: FrameLayout? = null
    private var mContentView: FrameLayout? = null
    private var mErrorView: FrameLayout? = null
    private var mErrorText: TextView? = null
    private var mRetryButton: Button? = null

    // Retry Callback Interface
    private var retryCallback: RetryCallback? = null

    /**
     * Interface to handle retry actions when the retry button is clicked.
     */
    interface RetryCallback {
        fun onRetry()
    }

    /**
     * Constructor for programmatically creating the view.
     *
     * @param context The Context the view is running in.
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context The Context the view is running in.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    /**
     * Initializes the LoadingViewFlipper by inflating the layout and setting up child views.
     *
     * @param context The Context the view is running in.
     */
    private fun init(context: Context) {
        Logger.d(TAG, "Initializing LoadingViewFlipper")
        try {
            // Inflate the layout into this ViewFlipper
            LayoutInflater.from(context).inflate(R.layout.loading_view_flipper, this, true)
            Logger.d(TAG, "Inflated loading_view_flipper layout successfully.")

            // Initialize child views
            mLoadingView = findViewById(R.id.loading_view)
            mContentView = findViewById(R.id.content_view)
            mErrorView = findViewById(R.id.error_view)
            mErrorText = findViewById(R.id.error_text)
            mRetryButton = findViewById(R.id.retry_button)

            // Verify that all child views are found
            if (mLoadingView == null || mContentView == null || mErrorView == null || mErrorText == null || mRetryButton == null) {
                Logger.e(TAG, "One or more child views not found in loading_view_flipper.xml")
            } else {
                Logger.v(TAG, "All child views initialized successfully.")

                // Set up Retry Button click listener
                mRetryButton?.setOnClickListener { _: View? ->
                    Logger.d(TAG, "Retry button clicked.")
                    showLoading()
                    if (retryCallback != null) {
                        retryCallback!!.onRetry()
                    } else {
                        Logger.e(TAG, "RetryCallback not set.")
                    }
                }
                Logger.v(TAG, "Retry button click listener set.")
            }

            // Optional: Set up animations
            setupAnimations()
        } catch (e: Exception) {
            Logger.e(TAG, "Error during LoadingViewFlipper initialization.", e)
        }
    }

    /**
     * Sets up fade-in and fade-out animations for the ViewFlipper.
     */
    private fun setupAnimations() {
        try {
            val animationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
            Logger.v(TAG, "Setting up animations with duration: " + animationDuration + "ms")

            // Fade In Animation
            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.interpolator = LinearInterpolator()
            fadeIn.duration = animationDuration.toLong()
            inAnimation = fadeIn
            Logger.v(TAG, "FadeIn animation set.")

            // Fade Out Animation
            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.interpolator = LinearInterpolator()
            fadeOut.duration = animationDuration.toLong()
            outAnimation = fadeOut
            Logger.v(TAG, "FadeOut animation set.")
        } catch (e: Exception) {
            Logger.e(TAG, "Error setting up animations.", e)
        }
    }

    /**
     * Displays the loading view.
     */
    fun showLoading() {
        Logger.d(TAG, "showLoading called.")
        if (displayedChild != LOADING_VIEW_INDEX) {
            displayedChild = LOADING_VIEW_INDEX
            Logger.v(TAG, "Switched to LOADING view.")
        }
    }

    /**
     * Displays the main content view.
     */
    fun showContent() {
        Logger.d(TAG, "showContent called.")
        if (displayedChild != CONTENT_VIEW_INDEX) {
            displayedChild = CONTENT_VIEW_INDEX
            Logger.v(TAG, "Switched to CONTENT view.")
        }
    }

    /**
     * Displays the error view.
     */
    fun showError() {
        Logger.d(TAG, "showError called.")
        if (displayedChild != ERROR_VIEW_INDEX) {
            displayedChild = ERROR_VIEW_INDEX
            Logger.v(TAG, "Switched to ERROR view.")
        }
    }

    /**
     * Displays the error view with a specific error message.
     *
     * @param errorMessage The error message to display.
     */
    fun showError(errorMessage: String?) {
        Logger.d(TAG, "showError(String) called with message: $errorMessage")
        setError(errorMessage)
        showError() // Switch to ERROR view
    }

    /**
     * Sets the error message in the error view.
     *
     * @param text The error message to display.
     */
    fun setError(text: String?) {
        if (mErrorText != null) {
            mErrorText!!.text = text
            Logger.v(TAG, "Error message set: $text")
        } else {
            Logger.e(TAG, "Error TextView not initialized.")
        }
    }

    /**
     * Sets the retry callback to handle retry actions when the retry button is clicked.
     *
     * @param callback The implementation of RetryCallback.
     */
    fun setRetryCallback(@Nullable callback: RetryCallback?) {
        retryCallback = callback
        if (callback != null) {
            Logger.d(TAG, "RetryCallback set.")
        } else {
            Logger.e(TAG, "RetryCallback set to null.")
        }
    }

    companion object {
        private val TAG = LoadingViewFlipper::class.java.simpleName
        private const val LOADING_VIEW_INDEX = 0
        private const val CONTENT_VIEW_INDEX = 1
        private const val ERROR_VIEW_INDEX = 2
    }
}
