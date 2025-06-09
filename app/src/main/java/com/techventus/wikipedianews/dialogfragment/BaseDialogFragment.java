package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.WikiApplication;

public abstract class BaseDialogFragment extends DialogFragment {
	private static final Typeface mTypeface;
	static
	{
		mTypeface = Typeface.createFromAsset(WikiApplication.getInstance().getAssets(), "fonts/Calibre-Semibold.otf");
	}

	@Override
	public void onStart()
	{
		super.onStart();
		AlertDialog alertDialog = (AlertDialog) getDialog();
		if (alertDialog != null)
		{
			updateButton(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE));
			updateButton(alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE));
		}
	}

	private void updateButton(final Button button)
	{
		if (button != null && getActivity() != null)
		{
			button.setTypeface(mTypeface);
			button.setTextColor(ContextCompat.getColor(getActivity(), R.color.notification_dialog_button));
		}
	}
}