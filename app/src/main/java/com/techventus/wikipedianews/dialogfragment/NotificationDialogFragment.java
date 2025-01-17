package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment; // Updated import
import com.techventus.wikipedianews.R;

import org.apache.commons.lang3.StringUtils;

public class NotificationDialogFragment extends DialogFragment
{
	public static final int ICON_NONE = 0;
	public static final int ICON_ALERT = 1;

	public static final int POSITIVE_BUTTON = 0;
	public static final int NEGATIVE_BUTTON = 1;

	protected static final String ICON_TYPE_TAG = "icon_resource_id";
	protected static final String TITLE_RESOURCE_ID_TAG = "title_resource_id";
	protected static final String TITLE_TAG = "title";
	protected static final String MESSAGE_RESOURCE_ID_TAG = "message_resource_id";
	protected static final String MESSAGE_TAG = "message";
	protected static final String POSITIVE_BUTTON_RESOURCE_ID_TAG = "positive_button_resource_id";
	protected static final String NEGATIVE_BUTTON_RESOURCE_ID_TAG = "negative_button_resource_id";
	protected static final String POSITIVE_BUTTON_TAG = "positive_button";
	protected static final String NEGATIVE_BUTTON_TAG = "negative_button";

	/**
	 * Returns a Notification Fragment configured to display a title and body without icon
	 * @param titleResourceId title resource ID (may be 0 for no title)
	 * @param messageResourceId body resource ID
	 * @param positiveButtonResourceId positive button resource ID
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId)
	{
		return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, -1);
	}

	/**
	 * Returns a Notification Fragment configured to display a title and body without icon
	 * @param titleResourceId title resource ID (may be 0 for no title)
	 * @param messageResourceId body resource ID
	 * @param positiveButtonResourceId positive button resource ID
	 * @param negativeButtonResourceId negative button resource ID
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId,
														 int negativeButtonResourceId)
	{
		return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, negativeButtonResourceId);
	}

	/**
	 * Returns a Notification Fragment configured to display a title and body with an icon as per the constants above
	 * @param iconType one of the constants above
	 * @param titleResourceId title resource ID (may be 0 for no title)
	 * @param messageResourceId body resource ID
	 * @param positiveButtonResourceId positive button resource ID
	 * @param negativeButtonResourceId negative button resource ID
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(int iconType, int titleResourceId, int messageResourceId,
														 int positiveButtonResourceId, int negativeButtonResourceId)
	{
		NotificationDialogFragment fragment = new NotificationDialogFragment();

		Bundle args = new Bundle();
		args.putInt(ICON_TYPE_TAG, iconType);
		args.putInt(TITLE_RESOURCE_ID_TAG, titleResourceId);
		args.putInt(MESSAGE_RESOURCE_ID_TAG, messageResourceId);
		args.putInt(POSITIVE_BUTTON_RESOURCE_ID_TAG, positiveButtonResourceId);
		args.putInt(NEGATIVE_BUTTON_RESOURCE_ID_TAG, negativeButtonResourceId);
		fragment.setArguments(args);

		return fragment;
	}

	/**
	 * Returns a Notification Fragment configured to display a title and body with an icon as per the constants above
	 * @param title dialog title, may be null
	 * @param message dialog body
	 * @param positiveButton positive button label
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(String title, String message, String positiveButton)
	{
		return newInstance(ICON_NONE, title, message, positiveButton, null);
	}

	/**
	 * Returns a Notification Fragment configured to display a title and body with an icon as per the constants above
	 * @param title dialog title, may be null
	 * @param message dialog body
	 * @param positiveButton positive button label
	 * @param negativeButton negative button label
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(String title, String message, String positiveButton,
														 String negativeButton)
	{
		return newInstance(ICON_NONE, title, message, positiveButton, negativeButton);
	}

	/**
	 * Returns a Notification Fragment configured to display a title and body without icon
	 * @param iconType one of the constants above
	 * @param title dialog title, may be null
	 * @param message dialog body
	 * @param positiveButton positive button label
	 * @param negativeButton negative button label
	 * @return the requested fragment
	 */
	public static NotificationDialogFragment newInstance(int iconType, String title, String message,
														 String positiveButton, String negativeButton)
	{
		NotificationDialogFragment fragment = new NotificationDialogFragment();

		Bundle args = new Bundle();
		args.putInt(ICON_TYPE_TAG, iconType);
		args.putString(TITLE_TAG, title);
		args.putString(MESSAGE_TAG, message);
		args.putString(POSITIVE_BUTTON_TAG, positiveButton);
		if(StringUtils.isNotEmpty(negativeButton))
		{
			args.putString(NEGATIVE_BUTTON_TAG, negativeButton);
		}
		fragment.setArguments(args);

		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Bundle bundle = getArguments();
		int iconType = bundle.getInt(ICON_TYPE_TAG, ICON_NONE);
		int titleResourceId = bundle.getInt(TITLE_RESOURCE_ID_TAG, 0);
		String title = titleResourceId > 0 ? getString(titleResourceId) : bundle.getString(TITLE_TAG);
		int messageResourceId = bundle.getInt(MESSAGE_RESOURCE_ID_TAG, 0);
		String message = messageResourceId > 0 ? getString(messageResourceId) : bundle.getString(MESSAGE_TAG, null);
		int positiveButtonResourceId = bundle.getInt(POSITIVE_BUTTON_RESOURCE_ID_TAG);
		String positiveButton = positiveButtonResourceId > 0 ? getString(positiveButtonResourceId) : bundle.getString(POSITIVE_BUTTON_TAG);
		int negativeButtonResourceId = bundle.getInt(NEGATIVE_BUTTON_RESOURCE_ID_TAG);
		String negativeButton = null;
		if(negativeButtonResourceId >0)
		{
			negativeButton = getString(negativeButtonResourceId);
		}
		else if(bundle.containsKey(NEGATIVE_BUTTON_TAG) && StringUtils.isNotEmpty(bundle.getString(NEGATIVE_BUTTON_TAG)))
		{
			negativeButton = bundle.getString(NEGATIVE_BUTTON_TAG);
		}

		final View view = View.inflate(getActivity(), R.layout.notification_dialog_fragment, null);
		TextView textView = (TextView) view.findViewById(R.id.dialog_title);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (!TextUtils.isEmpty(title))
		{
			textView.setText(title);
			int resourceId;
			switch (iconType)
			{
				case ICON_ALERT:
					resourceId = R.drawable.ic_error_36dp;
					break;
				case ICON_NONE:
				default:
					resourceId = -1;
					break;
			}
			if (resourceId > 0)
			{
				textView.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
				textView.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.notification_drawable_padding));
			}
		}
		else
		{
			textView.setVisibility(View.GONE);
		}
		textView = (TextView) view.findViewById(R.id.dialog_body);
		textView.setText(message);
		builder.setPositiveButton(positiveButton, (dialog, which) -> { // Inline the OnClickListener
			if (getTargetFragment() != null) {
				Intent intent = getPositveButtonIntent(); // Call the Intent method
				if(intent == null)
				{
					intent = new Intent(); // create a new intent if there is no intent returned.
				}
				getTargetFragment().onActivityResult(getTargetRequestCode(), POSITIVE_BUTTON, intent);
			}
		});
		if (!TextUtils.isEmpty(negativeButton))
		{
			builder.setNegativeButton(negativeButton, (dialog, which) -> {
				if (getTargetFragment() != null)
				{
					getTargetFragment().onActivityResult(getTargetRequestCode(), NEGATIVE_BUTTON, getNegativeButtonIntent());
				}
			});
		}
		builder.setView(view);
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setCancelable(false);
		alertDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);

		return alertDialog;
	}

	/**
	 * Method to be overriden if data should be returned to calling Parent
	 * when Positive Button is clicked
	 * @return Intent returned to instantiting onActivityResult
	 */
	protected Intent getPositveButtonIntent()
	{
		return null;
	}

	/**
	 * Method to be overriden if data should be returned to calling Parent
	 * when Negative Button is clicked
	 * @return Intent returned to instantiting onActivityResult
	 */
	protected Intent getNegativeButtonIntent()
	{
		return null;
	}

}

