package com.techventus.wikipedianews.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.logging.Logger;

/**
 * Custom ViewFlipper to manage loading, content, and error views with retry functionality.
 */
public class LoadingViewFlipper extends ViewFlipper {
	private static final String TAG = LoadingViewFlipper.class.getSimpleName();

	// Child Views
	private FrameLayout mLoadingView;
	private FrameLayout mContentView;
	private FrameLayout mErrorView;
	private TextView mErrorText;
	private Button mRetryButton;

	// Retry Callback Interface
	private RetryCallback retryCallback;

	/**
	 * Interface to handle retry actions when the retry button is clicked.
	 */
	public interface RetryCallback {
		void onRetry();
	}

	/**
	 * Constructor for programmatically creating the view.
	 *
	 * @param context The Context the view is running in.
	 */
	public LoadingViewFlipper(Context context) {
		super(context);
		init(context);
	}

	/**
	 * Constructor that is called when inflating the view from XML.
	 *
	 * @param context The Context the view is running in.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 */
	public LoadingViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Initializes the LoadingViewFlipper by inflating the layout and setting up child views.
	 *
	 * @param context The Context the view is running in.
	 */
	private void init(@NonNull Context context) {
		Logger.d(TAG, "Initializing LoadingViewFlipper");

		try {
			// Inflate the layout into this ViewFlipper
			LayoutInflater.from(context).inflate(R.layout.loading_view_flipper, this, true);
			Logger.d(TAG, "Inflated loading_view_flipper layout successfully.");

			// Initialize child views
			mLoadingView = findViewById(R.id.loading_view);
			mContentView = findViewById(R.id.content_view);
			mErrorView = findViewById(R.id.error_view);
			mErrorText = findViewById(R.id.error_text);
			mRetryButton = findViewById(R.id.retry_button);

			// Verify that all child views are found
			if (mLoadingView == null || mContentView == null || mErrorView == null || mErrorText == null || mRetryButton == null) {
				Logger.e(TAG, "One or more child views not found in loading_view_flipper.xml");
			} else {
				Logger.v(TAG, "All child views initialized successfully.");

				// Set up Retry Button click listener
				mRetryButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Logger.d(TAG, "Retry button clicked.");
						showLoading();
						if (retryCallback != null) {
							retryCallback.onRetry();
						} else {
							Logger.e(TAG, "RetryCallback not set.");
						}
					}
				});
				Logger.v(TAG, "Retry button click listener set.");
			}

			// Optional: Set up animations
			setupAnimations();

		} catch (Exception e) {
			Logger.e(TAG, "Error during LoadingViewFlipper initialization.", e);
		}
	}

	/**
	 * Sets up fade-in and fade-out animations for the ViewFlipper.
	 */
	private void setupAnimations() {
		try {
			int animationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			Logger.v(TAG, "Setting up animations with duration: " + animationDuration + "ms");

			// Fade In Animation
			AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
			fadeIn.setInterpolator(new LinearInterpolator());
			fadeIn.setDuration(animationDuration);
			setInAnimation(fadeIn);
			Logger.v(TAG, "FadeIn animation set.");

			// Fade Out Animation
			AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
			fadeOut.setInterpolator(new LinearInterpolator());
			fadeOut.setDuration(animationDuration);
			setOutAnimation(fadeOut);
			Logger.v(TAG, "FadeOut animation set.");
		} catch (Exception e) {
			Logger.e(TAG, "Error setting up animations.", e);
		}
	}

	/**
	 * Displays the loading view.
	 */
	public void showLoading() {
		Logger.d(TAG, "showLoading called.");
		if (getDisplayedChild() != 0) {
			setDisplayedChild(0);
			Logger.v(TAG, "Switched to LOADING view.");
		}
	}

	/**
	 * Displays the main content view.
	 */
	public void showContent() {
		Logger.d(TAG, "showContent called.");
		if (getDisplayedChild() != 1) {
			setDisplayedChild(1);
			Logger.v(TAG, "Switched to CONTENT view.");
		}
	}

	/**
	 * Displays the error view.
	 */
	public void showError() {
		Logger.d(TAG, "showError called.");
		if (getDisplayedChild() != 2) {
			setDisplayedChild(2);
			Logger.v(TAG, "Switched to ERROR view.");
		}
	}

	/**
	 * Displays the error view with a specific error message.
	 *
	 * @param errorMessage The error message to display.
	 */
	public void showError(String errorMessage) {
		Logger.d(TAG, "showError(String) called with message: " + errorMessage);
		setError(errorMessage);
		showError(); // Switch to ERROR view
	}

	/**
	 * Sets the error message in the error view.
	 *
	 * @param text The error message to display.
	 */
	public void setError(final String text) {
		if (mErrorText != null) {
			mErrorText.setText(text);
			Logger.v(TAG, "Error message set: " + text);
		} else {
			Logger.e(TAG, "Error TextView not initialized.");
		}
	}

	/**
	 * Sets the retry callback to handle retry actions when the retry button is clicked.
	 *
	 * @param callback The implementation of RetryCallback.
	 */
	public void setRetryCallback(@Nullable RetryCallback callback) {
		this.retryCallback = callback;
		if (callback != null) {
			Logger.d(TAG, "RetryCallback set.");
		} else {
			Logger.e(TAG, "RetryCallback set to null.");
		}
	}
}
