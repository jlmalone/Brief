package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.techventus.wikipedianews.R;

public class GenericProgressDialogFragment extends DialogFragment
{
	private static final String MESSAGE_RESOURCE_ID_TAG = "message_resource_id";
	private static final String MESSAGE_TAG = "message";
	private static final String CANCELLABLE_TAG = "cancellable";

	public static GenericProgressDialogFragment newInstance(int titleResourceId)
	{
		return newInstance(titleResourceId, true);
	}

	public static GenericProgressDialogFragment newInstance(int titleResourceId, boolean cancellable)
	{
		GenericProgressDialogFragment fragment = new GenericProgressDialogFragment();

		Bundle args = new Bundle();
		args.putInt(MESSAGE_RESOURCE_ID_TAG, titleResourceId);
		args.putBoolean(CANCELLABLE_TAG, cancellable);
		fragment.setArguments(args);

		return fragment;
	}

	public static GenericProgressDialogFragment newInstance(String message)
	{
		return newInstance(message, true);
	}

	public static GenericProgressDialogFragment newInstance(String message, boolean cancellable)
	{
		GenericProgressDialogFragment fragment = new GenericProgressDialogFragment();

		Bundle args = new Bundle();
		args.putString(MESSAGE_TAG, message);
		args.putBoolean(CANCELLABLE_TAG, cancellable);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Bundle bundle = getArguments();
		int messageResourceId = bundle.getInt(MESSAGE_RESOURCE_ID_TAG, 0);
		String message = bundle.getString(MESSAGE_TAG, null);
		boolean cancellable = bundle.getBoolean(CANCELLABLE_TAG, true);
		final View view = View.inflate(getActivity(), R.layout.progress_dialog_fragment, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		TextView textView = (TextView) view.findViewById(R.id.progress_text);
		if (messageResourceId > 0)
		{
			textView.setText(messageResourceId);
		}
		else if (!TextUtils.isEmpty(message))
		{
			textView.setText(message);
		}
		builder.setView(view);
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(cancellable);
		alertDialog.setCancelable(cancellable);
		if (!cancellable)
		{
			alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
			{
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
				{
					return keyCode == KeyEvent.KEYCODE_BACK;
				}
			});
		}
		return alertDialog;
	}
}
