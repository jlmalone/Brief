package com.techventus.wikipedianews.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.techventus.wikipedianews.R
import org.apache.commons.lang3.StringUtils

open class NotificationDialogFragment : DialogFragment() {

    interface NotificationDialogListener {
        fun onPositiveButtonClicked(intent: Intent)
        fun onNegativeButtonClicked(intent: Intent)
    }

    private var listener: NotificationDialogListener? = null

    // Factory methods
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment: Fragment? = parentFragment
        if (parentFragment is NotificationDialogListener) {
            listener = parentFragment
        } else {
            throw RuntimeException(
                parentFragment.toString()
                        + " must implement NotificationDialogListener"
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val iconType = bundle?.getInt(ICON_TYPE_TAG, ICON_NONE) ?: ICON_NONE
        val titleResourceId = bundle?.getInt(TITLE_RESOURCE_ID_TAG, 0) ?: 0
        val title = if (titleResourceId > 0) getString(titleResourceId) else bundle?.getString(TITLE_TAG)
        val messageResourceId = bundle?.getInt(MESSAGE_RESOURCE_ID_TAG, 0) ?: 0
        val message = if (messageResourceId > 0) getString(messageResourceId) else bundle?.getString(MESSAGE_TAG, null)
        val positiveButtonResourceId = bundle?.getInt(POSITIVE_BUTTON_RESOURCE_ID_TAG) ?: 0
        val positiveButton = if (positiveButtonResourceId > 0) getString(positiveButtonResourceId) else bundle?.getString(POSITIVE_BUTTON_TAG)
        val negativeButtonResourceId = bundle?.getInt(NEGATIVE_BUTTON_RESOURCE_ID_TAG) ?: 0
        var negativeButton: String? = null
        if (negativeButtonResourceId > 0) {
            negativeButton = getString(negativeButtonResourceId)
        } else if (bundle?.containsKey(NEGATIVE_BUTTON_TAG) == true && StringUtils.isNotEmpty(bundle.getString(NEGATIVE_BUTTON_TAG))) {
            negativeButton = bundle.getString(NEGATIVE_BUTTON_TAG)
        }

        val view = View.inflate(activity, R.layout.notification_dialog_fragment, null)
        val titleView = view.findViewById<TextView>(R.id.dialog_title)
        val builder = AlertDialog.Builder(activity)
        if (!TextUtils.isEmpty(title)) {
            titleView.text = title
            val resourceId: Int = when (iconType) {
                ICON_ALERT -> R.drawable.ic_error_36dp
                ICON_NONE -> -1
                else -> -1
            }
            if (resourceId > 0) {
                titleView.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0)
                titleView.compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.notification_drawable_padding)
            }
        } else {
            titleView.visibility = View.GONE
        }
        val messageView = view.findViewById<TextView>(R.id.dialog_body)
        messageView.text = message
        builder.setPositiveButton(positiveButton) { _, _ ->
            listener?.let {
                var intent = positiveButtonIntent
                if (intent == null) {
                    intent = Intent()
                }
                it.onPositiveButtonClicked(intent)
            }
        }
        if (!TextUtils.isEmpty(negativeButton)) {
            builder.setNegativeButton(negativeButton) { _, _ ->
                listener?.let {
                    var intent = negativeButtonIntent
                    if (intent == null) {
                        intent = Intent()
                    }
                    it.onNegativeButtonClicked(intent)
                }
            }
        }
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
        return alertDialog
    }

    /**
     * Method to be overridden if data should be returned to calling Parent
     * when Positive Button is clicked
     * @return Intent returned to instantiating onActivityResult
     */
    protected open val positiveButtonIntent: Intent?
        get() = null

    /**
     * Method to be overridden if data should be returned to calling Parent
     * when Negative Button is clicked
     * @return Intent returned to instantiating onActivityResult
     */
    protected open val negativeButtonIntent: Intent?
        get() = null

    companion object {
        const val ICON_NONE = 0
        const val ICON_ALERT = 1
        const val POSITIVE_BUTTON = 0
        const val NEGATIVE_BUTTON = 1
        const val ICON_TYPE_TAG = "icon_resource_id"
        const val TITLE_RESOURCE_ID_TAG = "title_resource_id"
        const val TITLE_TAG = "title"
        const val MESSAGE_RESOURCE_ID_TAG = "message_resource_id"
        const val MESSAGE_TAG = "message"
        const val POSITIVE_BUTTON_RESOURCE_ID_TAG = "positive_button_resource_id"
        const val NEGATIVE_BUTTON_RESOURCE_ID_TAG = "negative_button_resource_id"
        const val POSITIVE_BUTTON_TAG = "positive_button"
        const val NEGATIVE_BUTTON_TAG = "negative_button"

        @JvmStatic
        fun newInstance(titleResourceId: Int, messageResourceId: Int, positiveButtonResourceId: Int): NotificationDialogFragment {
            return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, -1)
        }

        @JvmStatic
        fun newInstance(titleResourceId: Int, messageResourceId: Int, positiveButtonResourceId: Int, negativeButtonResourceId: Int): NotificationDialogFragment {
            return newInstance(ICON_NONE, titleResourceId, messageResourceId, positiveButtonResourceId, negativeButtonResourceId)
        }

        @JvmStatic
        fun newInstance(iconType: Int, titleResourceId: Int, messageResourceId: Int, positiveButtonResourceId: Int, negativeButtonResourceId: Int): NotificationDialogFragment {
            val fragment = NotificationDialogFragment()
            val args = Bundle()
            args.putInt(ICON_TYPE_TAG, iconType)
            args.putInt(TITLE_RESOURCE_ID_TAG, titleResourceId)
            args.putInt(MESSAGE_RESOURCE_ID_TAG, messageResourceId)
            args.putInt(POSITIVE_BUTTON_RESOURCE_ID_TAG, positiveButtonResourceId)
            args.putInt(NEGATIVE_BUTTON_RESOURCE_ID_TAG, negativeButtonResourceId)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(title: String?, message: String?, positiveButton: String?): NotificationDialogFragment {
            return newInstance(ICON_NONE, title, message, positiveButton, null)
        }

        @JvmStatic
        fun newInstance(title: String?, message: String?, positiveButton: String?, negativeButton: String?): NotificationDialogFragment {
            return newInstance(ICON_NONE, title, message, positiveButton, negativeButton)
        }

        @JvmStatic
        fun newInstance(iconType: Int, title: String?, message: String?, positiveButton: String?, negativeButton: String?): NotificationDialogFragment {
            val fragment = NotificationDialogFragment()
            val args = Bundle()
            args.putInt(ICON_TYPE_TAG, iconType)
            args.putString(TITLE_TAG, title)
            args.putString(MESSAGE_TAG, message)
            args.putString(POSITIVE_BUTTON_TAG, positiveButton)
            if (StringUtils.isNotEmpty(negativeButton)) {
                args.putString(NEGATIVE_BUTTON_TAG, negativeButton)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
