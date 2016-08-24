package com.techventus.wikipedianews.dialogfragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Build;
import android.widget.Button;
import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.WikiApplication;

public abstract class BaseDialogFragment extends DialogFragment
{
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

	@SuppressWarnings("deprecation")
	private void updateButton(final Button button)
	{
		if (button != null)
		{
			button.setTypeface(mTypeface);
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
			{
				button.setTextColor(getResources().getColor(R.color.notification_dialog_button, getActivity().getTheme()));
			}
			else
			{
				// deprecated in API 23
				button.setTextColor(getResources().getColor(R.color.notification_dialog_button));
			}
		}
	}
}
