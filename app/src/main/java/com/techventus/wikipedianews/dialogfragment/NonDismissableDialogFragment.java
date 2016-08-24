package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.techventus.wikipedianews.fragment.WikiFragment;
import org.apache.commons.lang3.StringUtils;

public class NonDismissableDialogFragment extends NotificationDialogFragment
{
	/**
	 * Returns a Notification Fragment configured to display a title and body without icon
	 * @param titleResourceId title resource ID (may be 0 for no title)
	 * @param messageResourceId body resource ID
	 * @param positiveButtonResourceId positive button resource ID
	 * @return the requested fragment
	 */
	public static NonDismissableDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId)
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
	public static NonDismissableDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId,
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
	public static NonDismissableDialogFragment newInstance(int iconType, int titleResourceId, int messageResourceId,
			int positiveButtonResourceId, int negativeButtonResourceId)
	{
		NonDismissableDialogFragment fragment = new NonDismissableDialogFragment();

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
	public static NonDismissableDialogFragment newInstance(String title, String message, String positiveButton)
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
	public static NonDismissableDialogFragment newInstance(String title, String message, String positiveButton,
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
	public static NonDismissableDialogFragment newInstance(int iconType, String title, String message,
			String positiveButton, String negativeButton)
	{
		NonDismissableDialogFragment fragment = new NonDismissableDialogFragment();

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

	@Override
	public void onStart()
	{
		super.onStart();
		AlertDialog alertDialog = (AlertDialog) getDialog();
		if(alertDialog != null)
		{
			Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (getTargetFragment() != null)
					{
						/**TODO replace with onActivityResult as per Francesc's suggestion in PR
						 *
						 Francesc V. Guell
						 I don't see what the risk is. Every fragment passes an identifier,
						 so as long as we honour that and return the identifier we were given, no functionality will be broken. All we have to do is default to the base fragment to handle the onActivityResult and call onErrorDialogDismissed.
						 */
						((WikiFragment)getTargetFragment()).handleOnClickForNonDismissableDialogs(getTargetRequestCode(), POSITIVE_BUTTON);
					}
				}
			});
			Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
			negativeButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (getTargetFragment() != null)
					{
						((WikiFragment)getTargetFragment()).handleOnClickForNonDismissableDialogs(getTargetRequestCode(), NEGATIVE_BUTTON);
					}
				}
			});
		}
	}
}
