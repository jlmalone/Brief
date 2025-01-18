package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.techventus.wikipedianews.R;

import org.apache.commons.lang3.StringUtils;

public class NotificationDialogFragment extends DialogFragment {

	public interface NotificationDialogListener {
		void onPositiveButtonClicked(Intent intent);
		void onNegativeButtonClicked(Intent intent);
	}

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

	private NotificationDialogListener listener;

	// Factory methods

	public static NotificationDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId) {
		return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, -1);
	}

	public static NotificationDialogFragment newInstance(int titleResourceId, int messageResourceId, int positiveButtonResourceId, int negativeButtonResourceId) {
		return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, negativeButtonResourceId);
	}

	public static NotificationDialogFragment newInstance(int iconType, int titleResourceId, int messageResourceId, int positiveButtonResourceId, int negativeButtonResourceId) {
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

	public static NotificationDialogFragment newInstance(String title, String message, String positiveButton) {
		return newInstance(ICON_NONE, title, message, positiveButton, null);
	}

	public static NotificationDialogFragment newInstance(String title, String message, String positiveButton, String negativeButton) {
		return newInstance(ICON_NONE, title, message, positiveButton, negativeButton);
	}

	public static NotificationDialogFragment newInstance(int iconType, String title, String message, String positiveButton, String negativeButton) {
		NotificationDialogFragment fragment = new NotificationDialogFragment();

		Bundle args = new Bundle();
		args.putInt(ICON_TYPE_TAG, iconType);
		args.putString(TITLE_TAG, title);
		args.putString(MESSAGE_TAG, message);
		args.putString(POSITIVE_BUTTON_TAG, positiveButton);
		if (StringUtils.isNotEmpty(negativeButton)) {
			args.putString(NEGATIVE_BUTTON_TAG, negativeButton);
		}
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		Fragment parentFragment = getParentFragment();
		if (parentFragment instanceof NotificationDialogListener) {
			listener = (NotificationDialogListener) parentFragment;
		} else {
			throw new RuntimeException(parentFragment.toString()
					+ " must implement NotificationDialogListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
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
		if (negativeButtonResourceId > 0) {
			negativeButton = getString(negativeButtonResourceId);
		} else if (bundle.containsKey(NEGATIVE_BUTTON_TAG) && StringUtils.isNotEmpty(bundle.getString(NEGATIVE_BUTTON_TAG))) {
			negativeButton = bundle.getString(NEGATIVE_BUTTON_TAG);
		}

		final View view = View.inflate(getActivity(), R.layout.notification_dialog_fragment, null);
		TextView titleView = view.findViewById(R.id.dialog_title);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (!TextUtils.isEmpty(title)) {
			titleView.setText(title);
			int resourceId;
			switch (iconType) {
				case ICON_ALERT:
					resourceId = R.drawable.ic_error_36dp;
					break;
				case ICON_NONE:
				default:
					resourceId = -1;
					break;
			}
			if (resourceId > 0) {
				titleView.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
				titleView.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.notification_drawable_padding));
			}
		} else {
			titleView.setVisibility(View.GONE);
		}
		TextView messageView = view.findViewById(R.id.dialog_body);
		messageView.setText(message);
		builder.setPositiveButton(positiveButton, (dialog, which) -> {
			if (listener != null) {
				Intent intent = getPositiveButtonIntent();
				if (intent == null) {
					intent = new Intent();
				}
				listener.onPositiveButtonClicked(intent);
			}
		});
		if (!TextUtils.isEmpty(negativeButton)) {
			builder.setNegativeButton(negativeButton, (dialog, which) -> {
				if (listener != null) {
					Intent intent = getNegativeButtonIntent();
					if (intent == null) {
						intent = new Intent();
					}
					listener.onNegativeButtonClicked(intent);
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
	 * Method to be overridden if data should be returned to calling Parent
	 * when Positive Button is clicked
	 * @return Intent returned to instantiating onActivityResult
	 */
	protected Intent getPositiveButtonIntent() {
		return null;
	}

	/**
	 * Method to be overridden if data should be returned to calling Parent
	 * when Negative Button is clicked
	 * @return Intent returned to instantiating onActivityResult
	 */
	protected Intent getNegativeButtonIntent() {
		return null;
	}
}
