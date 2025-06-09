package com.techventus.wikipedianews.dialogfragment

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.WikiApplication

abstract class BaseDialogFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()
        val alertDialog = dialog as AlertDialog?
        alertDialog?.let {
            updateButton(it.getButton(AlertDialog.BUTTON_POSITIVE))
            updateButton(it.getButton(AlertDialog.BUTTON_NEGATIVE))
        }
    }

    private fun updateButton(button: Button?) {
        button?.let {
            it.typeface = mTypeface
            activity?.let { currentActivity ->
                it.setTextColor(ContextCompat.getColor(currentActivity, R.color.notification_dialog_button))
            }
        }
    }

    companion object {
        private val mTypeface: Typeface = Typeface.createFromAsset(WikiApplication.getInstance()?.assets, "fonts/Calibre-Semibold.otf")
    }
}