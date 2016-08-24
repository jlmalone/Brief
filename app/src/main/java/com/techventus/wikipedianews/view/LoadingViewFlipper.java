package com.techventus.wikipedianews.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.techventus.wikipedianews.R;

/**
 * Created by josephmalone on 16-06-25.
 */
public class LoadingViewFlipper extends ViewFlipper
{
	private static final int LOADING_INDEX = 0;
	private static final int MAIN_CONTENT_INDEX = 1;
	private static final int ERROR_INDEX = 2;

	private ViewGroup mMainContainer;
	private TextView mErrorText;
	private int mWidth;

	public LoadingViewFlipper(Context context)
	{
		super(context);
		init(context);
	}

	public LoadingViewFlipper(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
	}

	public float getXFraction()
	{
		return mWidth > 0 ? getX() / mWidth : 0f;
	}

	public void setXFraction(float xFraction)
	{
		setX((mWidth > 0) ? (xFraction * mWidth) : -9999f);
	}

	private void init(final Context context)
	{
		inflate(context, R.layout.loading_view_flipper, this);
		mMainContainer = (ViewGroup) findViewById(R.id.loading_flipper_main_content);
		mErrorText = (TextView) findViewById(R.id.loading_flipper_error_text);
		int animationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
		Animation fadeIn = new AlphaAnimation(0f, 1f);
		Interpolator interpolator = new LinearInterpolator();
		fadeIn.setInterpolator(interpolator);
		fadeIn.setDuration(animationDuration);
		setInAnimation(fadeIn);
		Animation fadeOut = new AlphaAnimation(1f, 0f);
		fadeOut.setInterpolator(interpolator);
		fadeOut.setDuration(animationDuration);
		setOutAnimation(fadeOut);
	}

	public void showLoading()
	{
		if (getDisplayedChild() != LOADING_INDEX)
		{
			setDisplayedChild(LOADING_INDEX);
		}
	}

	public void showContent()
	{
		if (getDisplayedChild() != MAIN_CONTENT_INDEX)
		{
			setDisplayedChild(MAIN_CONTENT_INDEX);
		}
	}

	public void showError()
	{
		if (getDisplayedChild() != ERROR_INDEX)
		{
			setDisplayedChild(ERROR_INDEX);
		}
	}

	@Override
	public void addView(final View view)
	{
		if (mMainContainer != null)
		{
			mMainContainer.addView(view);
		}
		else
		{
			super.addView(view);
		}
	}

	@Override
	public void addView(final View view, final ViewGroup.LayoutParams layoutParams)
	{
		if (mMainContainer != null)
		{
			mMainContainer.addView(view, layoutParams);
		}
		else
		{
			super.addView(view, layoutParams);
		}
	}

	public void setError(final String text)
	{
		mErrorText.setText(text);
	}
}
