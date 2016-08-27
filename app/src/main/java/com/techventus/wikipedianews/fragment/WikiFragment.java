package com.techventus.wikipedianews.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.techventus.wikipedianews.BuildConfig;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.dialogfragment.GenericProgressDialogFragment;
import com.techventus.wikipedianews.dialogfragment.NonDismissableDialogFragment;
import com.techventus.wikipedianews.dialogfragment.NotificationDialogFragment;
import com.techventus.wikipedianews.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public abstract class WikiFragment extends Fragment
{
	private static final String TAG = WikiFragment.class.getSimpleName();
	private static final String ERROR_DIALOG_TAG = "wiki_frag_error_dialog";
	private static final String DIALOG_PAYLOAD_TAG = "payload";
	private static final String DIALOG_IDENTIFIER_TAG = "dialog_identifier";
	private static final String PROGRESS_DIALOG_TAG = "wiki_frag_progress_dialog";
	private static final String FORCE_UPGRADE_ERROR_DIALOG_TAG = "force_upgrade_error_dialog";

	private static final int ERROR_DIALOG_REQUEST_CODE = 54321;
	private static final int POWER_SAVER_DIALOG_CODE = 1234;
	protected static final String ERROR = "ERROR";
	private static final String GENERIC_ERROR = "GENERIC_ERROR";

//	protected final ApiManager mApiManager = ApiManager.getInstance();
	private String mTitle;
	private ToolbarPropertyCallback mToolbarCallback;
	private DialogFragment mErrorDialogFragment;
	private NonDismissableDialogFragment mForceUpgradeDialogFragment;
	private int mIdentifier;
	private Bundle mPayload;
	private GenericProgressDialogFragment mProgressDialog;
//	private static boolean mHasShownPowerModal = false;


	public interface ToolbarPropertyCallback
	{
		void setTitle(String title);

		void setNavigationContentDescription(String contentDescription);

		void setLogoDescription(String description);

		void setSearchEnabled(boolean enabled);

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null)
		{
			mIdentifier = savedInstanceState.getInt(DIALOG_IDENTIFIER_TAG);
			mPayload = savedInstanceState.getBundle(DIALOG_PAYLOAD_TAG);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(DIALOG_IDENTIFIER_TAG, mIdentifier);
		if (mPayload != null)
		{
			outState.putBundle(DIALOG_PAYLOAD_TAG, mPayload);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		FragmentManager fragmentManager = getFragmentManager();
		mErrorDialogFragment = (DialogFragment) fragmentManager.findFragmentByTag(ERROR_DIALOG_TAG);
		if (mErrorDialogFragment != null)
		{
			mErrorDialogFragment.setTargetFragment(this, ERROR_DIALOG_REQUEST_CODE);
		}
	}

	@Override
	public void onPause()
	{
		if (mErrorDialogFragment != null)
		{
			mErrorDialogFragment.setTargetFragment(null, 0);
			mErrorDialogFragment = null;
		}

		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case ERROR_DIALOG_REQUEST_CODE:
				mErrorDialogFragment = null;
				if (mIdentifier == POWER_SAVER_DIALOG_CODE)
				{
					if (resultCode == NotificationDialogFragment.POSITIVE_BUTTON)
					{
						try
						{
							startActivity(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
						}
						catch (Exception e)
						{
							//SHPA-599, SHPA-602 - TODO - get HTC and investigate if we can properly request the requisite permission for Battery Saver on HTC
						}
					}
				}
				else
				{
					onErrorDialogDismissed(mIdentifier, mPayload, resultCode);
					mPayload = null;
				}
				break;
		}
	}

	protected void setTitle(int titleResourceId)
	{
		mTitle = getResources().getString(titleResourceId);
		setTitle(mTitle);
	}

	/**
	 * Called when a dialog has been dismissed. To be overridden in child classes
	 */
	protected void onErrorDialogDismissed(int identifier, Bundle data, int buttonId)
	{
	}

	public void handleOnClickForNonDismissableDialogs(int identifier, int buttonId)
	{
	}

	/**
	 * Method to set the title of the fragments parent toolbar to the given title.
	 * if the parent activity does not implement the callback interface this call is ignored.
	 *
	 * @param title @NotNull String containing the title that we want to set in the toolbar.
	 */
	protected void setTitle(String title)
	{
		mTitle = title;
		if (title != null)
		{
			//If the parent activity is registered to receive callbacks. Set the title.
			if (mToolbarCallback != null)
			{
				mToolbarCallback.setTitle(title);
			}
		}
		else
		{
			Logger.e(TAG, "Error caller passed null to Set toolbar title");
			Logger.logStackTrace(TAG, new Exception());
		}
	}



	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		//If the activity that hosts this fragment implements the callbacks to set the toolbar properties.
		//we register it so that we can use them from within the fragment.
		if (context instanceof ToolbarPropertyCallback)
		{
			Logger.d(TAG, context + " implements toolbar callbacks. Registering");
			mToolbarCallback = (ToolbarPropertyCallback) context;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		//If the activity that hosts this fragment implements the callbacks to set the toolbar properties.
		//we register it so that we can use them from within the fragment.
		if (activity instanceof ToolbarPropertyCallback)
		{
			Logger.d(TAG, activity + " implements toolbar callbacks. Registering");
			mToolbarCallback = (ToolbarPropertyCallback) activity;
		}
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		//If the fragment has been detached from the parent activity then we need to deregister the callback.
		//If it is reattached later on that will be picked up in onAttach.
		mToolbarCallback = null;
	}

	/**
	 * Called by the activity to check whether the fragment wants to consume a back key presss
	 *
	 * @return true if the event is consumed, false otherwise
	 */
	public boolean onBackPressed()
	{
		return false;
	}

	/**
	 * Shows a snackbar with the app theme
	 *
	 * @param view       The view to find a parent from.
	 * @param resourceId The resource id of the string resource to use. Can be formatted text.
	 * @param length     How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 */
	protected void showSnackBar(final View view, int resourceId, int length)
	{
		showSnackBar(view, resourceId, length, -1);
	}

	/**
	 * Shows a snackbar with the app theme
	 *
	 * @param view       The view to find a parent from.
	 * @param resourceId The resource id of the string resource to use. Can be formatted text.
	 * @param length     How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
	 * @param color      background color for the snackbar (resource ID)
	 */
	@SuppressWarnings("deprecation")
	protected void showSnackBar(final View view, int resourceId, int length, int color)
	{
		Snackbar snackbar = Snackbar.make(view, resourceId, length);
		if (color > 0)
		{
			View snackBarView = snackbar.getView();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
			{
				snackBarView.setBackgroundColor(getResources().getColor(color, getContext().getTheme()));
			}
			else
			{
				snackBarView.setBackgroundColor(getResources().getColor(color));
			}
		}
		snackbar.show();
	}

	/**
	 * Call the activity callback that sets the content description. This Method takes an android
	 * resource id and converts it to a string to call the callback method of the same name.
	 *
	 * @param contentDescriptionResourceId @NotNull The Resource ID of the string that we would like to set
	 *                                     as the content description.
	 */
	void setNavigationContentDescription(int contentDescriptionResourceId)
	{
		if (mToolbarCallback != null)
		{
			mToolbarCallback.setNavigationContentDescription(getString(contentDescriptionResourceId));
		}
	}



	void setSearchEnabled(boolean enabled)
	{
		if (mToolbarCallback != null)
		{
			mToolbarCallback.setSearchEnabled(enabled);
		}
	}

	/**
	 * Call the activity callback that sets the logo description. This Method takes an android
	 * resource id and converts it to a string to call the callback method of the same name.
	 *
	 * @param logoDescriptionResourceId @NotNull The Resource ID of the string that we would like to set
	 *                                  as the logo description.
	 */
	void setLogoDescription(int logoDescriptionResourceId)
	{
		if (mToolbarCallback != null)
		{
			mToolbarCallback.setLogoDescription(getString(logoDescriptionResourceId));
		}
	}

	/**
	 * Shows a network error dialog
	 *
	 * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 */
	protected void showNoNetworkError(int identifier)
	{
		showNoNetworkError(identifier, true);
	}

	/**
	 * Shows a network error dialog with only a retry button. Used in situations such as the onboarding screen where cancel does not make sense.
	 *
	 * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 */
	protected void showNoNetworkError(int identifier, boolean showCancel)
	{
		if (showCancel)
		{
			showError(identifier, null, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection, R.string.cancel);
		}
		else
		{
			showError(identifier, null, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection);
		}
	}

	/**
	 * Shows a network error dialog
	 *
	 * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param data       a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 */
	protected void showNoNetworkError(int identifier, final Bundle data)
	{
		showError(identifier, data, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection, R.string.cancel);
	}


	/**
	 * Shows a notification error dialog
	 *
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleResId          the resource ID to use for the dialog title
	 * @param bodyResId           the resource ID to use for the dialog message
	 * @param positiveButtonResId the resource ID to use for the positive button
	 */
	protected void showError(int identifier, int titleResId, int bodyResId, int positiveButtonResId)
	{
		showError(identifier, null, titleResId, bodyResId, positiveButtonResId, -1);
	}

	/**
	 * Shows a notification error dialog
	 *
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleResId          the resource ID to use for the dialog title
	 * @param bodyResId           the resource ID to use for the dialog message
	 * @param positiveButtonResId the resource ID to use for the positive button
	 * @param negativeButtonResId the resource ID to use for the negative button
	 */
	public void showError(int identifier, int titleResId, int bodyResId, int positiveButtonResId, int negativeButtonResId)
	{
		showError(identifier, null, titleResId, bodyResId, positiveButtonResId, negativeButtonResId);
	}

	/**
	 * Shows a notification error dialog
	 *
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param data                a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleResId          the resource ID to use for the dialog title
	 * @param bodyResId           the resource ID to use for the dialog message
	 * @param positiveButtonResId the resource ID to use for the positive button
	 */
	protected void showError(int identifier, final Bundle data, int titleResId, int bodyResId, int positiveButtonResId)
	{
		showError(identifier, data, titleResId, bodyResId, positiveButtonResId, -1);
	}

	/**
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param data                a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleString         the dialog title string
	 * @param bodyString          the dialog body string
	 * @param positiveButtonResId the resource ID to use for the positive button
	 */
	protected void showError(int identifier, final Bundle data, String titleString, String bodyString, int positiveButtonResId)
	{
		showError(identifier, data, titleString, bodyString, positiveButtonResId, -1);
	}

	/**
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param data                a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleString         the dialog title string
	 * @param bodyString          the dialog body string
	 * @param positiveButtonResId the resource ID to use for the positive button
	 * @param negativeButtonResId the resource ID to use for the negative button
	 */
	protected void showError(int identifier, final Bundle data, String titleString, String bodyString, int positiveButtonResId, int negativeButtonResId)
	{
		genericError(identifier, data, titleString, -1, bodyString, -1, positiveButtonResId, negativeButtonResId);
	}

	private void genericError(int identifier, final Bundle data, String titleString, int titleResId, String bodyString, int bodyResId, int positiveButtonResId,
			int negativeButtonResId)
	{
		mIdentifier = identifier;
		mPayload = data;
		if (mErrorDialogFragment == null)
		{
			if ((StringUtils.isNotEmpty(titleString) && titleResId == -1) || (StringUtils.isNotEmpty(bodyString) && bodyResId == -1))
			{
				mErrorDialogFragment = NotificationDialogFragment.newInstance(NotificationDialogFragment.ICON_ALERT, titleString, bodyString,
						getString(positiveButtonResId), negativeButtonResId > 0 ? getString(negativeButtonResId) : null);
			}
			else
			{
				mErrorDialogFragment = NotificationDialogFragment.newInstance(NotificationDialogFragment.ICON_ALERT, titleResId, bodyResId,
						positiveButtonResId,
						negativeButtonResId);
			}
			//saw a crash in dev where mErrorDialogFragment was null after being instatiated
			if (mErrorDialogFragment != null && getFragmentManager() != null)
			{
				FragmentManager fragmentManager = getFragmentManager();
				if (fragmentManager != null)
				{
					mErrorDialogFragment.setTargetFragment(this, ERROR_DIALOG_REQUEST_CODE);
					mErrorDialogFragment.show(fragmentManager, ERROR_DIALOG_TAG);
				}
				else
				{
					if (BuildConfig.DEBUG)
					{
						throw new IllegalStateException("Attempted to display error dialog with null Fragment Manager");
					}
				}
			}
			else
			{
			}

		}
		else
		{
			if (BuildConfig.DEBUG)
			{
				//				throw new IllegalStateException("Attempting to display an error dialog when one already exists");
			}
			else
			{
				Logger.e(TAG, "Attempting to display an error dialog when one already exists");
			}
		}
	}

	/**
	 * Shows a notification error dialog
	 *
	 * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param data                a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleResId          the resource ID to use for the dialog title
	 * @param bodyResId           the resource ID to use for the dialog message
	 * @param positiveButtonResId the resource ID to use for the positive button
	 * @param negativeButtonResId the resource ID to use for the negative button
	 */
	protected void showError(int identifier, final Bundle data, int titleResId, int bodyResId, int positiveButtonResId, int negativeButtonResId)
	{
		genericError(identifier, data, null, titleResId, null, bodyResId, positiveButtonResId, negativeButtonResId);
	}

	/**
	 * @param identifier     an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
	 * @param titleString    the dialog title string
	 * @param bodyString     the dialog body string
	 * @param positiveButton the string to use for the positive button
	 * @param negativeButton the string to use for the negative button
	 */
	public void showForceUpgradeError(int identifier, String titleString, String bodyString, String positiveButton, String negativeButton)
	{
		if (mForceUpgradeDialogFragment == null)
		{
			mForceUpgradeDialogFragment = NonDismissableDialogFragment.newInstance(titleString, bodyString, positiveButton, negativeButton);
			mForceUpgradeDialogFragment.setTargetFragment(this, identifier);
			mForceUpgradeDialogFragment.show(getFragmentManager(), FORCE_UPGRADE_ERROR_DIALOG_TAG);
			mForceUpgradeDialogFragment.setCancelable(false);
		}
		else
		{
			if (BuildConfig.DEBUG)
			{
				throw new IllegalStateException("Attempting to display an error dialog when one already exists");
			}
			else
			{
				Logger.e(TAG, "Attempting to display an error dialog when one already exists");
			}
		}
	}

	/**
	 * Shows a progress dialog with the prompt as per the resource ID
	 *
	 * @param messageResId the resource ID of the prompt to display in the progress dialog
	 * @param cancelable   whether the user can cancel the dialog
	 */
	public void showProgress(int messageResId, boolean cancelable)
	{
		dismissProgress();
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null)
		{
			mProgressDialog = GenericProgressDialogFragment.newInstance(messageResId, cancelable);
			mProgressDialog.show(fragmentManager, PROGRESS_DIALOG_TAG);
		}
		else
		{
			if (BuildConfig.DEBUG)
			{
				throw new IllegalStateException("Attempted to show progress dialog with null Fragment Manager");
			}
		}
	}

	/**
	 * Dismisses the progress dialog. Safe to call if no progress dialog is being displayed.
	 */
	public void dismissProgress()
	{
		if (mProgressDialog != null && mProgressDialog.isAdded())
		{
			mProgressDialog.dismissAllowingStateLoss();
			mProgressDialog = null;
		}
	}

}
