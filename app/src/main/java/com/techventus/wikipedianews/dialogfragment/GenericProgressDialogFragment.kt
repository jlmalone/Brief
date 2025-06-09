package com.techventus.wikipedianews.dialogfragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.techventus.wikipedianews.R

class GenericProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        val messageResourceId = bundle?.getInt(MESSAGE_RESOURCE_ID_TAG, 0) ?: 0
        val message = bundle?.getString(MESSAGE_TAG, null)
        val cancellable = bundle?.getBoolean(CANCELLABLE_TAG, true) ?: true
        val view = View.inflate(activity, R.layout.progress_dialog_fragment, null)
        val builder = AlertDialog.Builder(activity)
        val textView = view.findViewById<TextView>(R.id.progress_text)
        if (messageResourceId > 0) {
            textView.setText(messageResourceId)
        } else if (!TextUtils.isEmpty(message)) {
            textView.text = message
        }
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(cancellable)
        alertDialog.setCancelable(cancellable)
        if (!cancellable) {
            alertDialog.setOnKeyListener { _: DialogInterface?, keyCode: Int, _: KeyEvent? -> keyCode == KeyEvent.KEYCODE_BACK }
        }
        return alertDialog
    }

    companion object {
        private const val MESSAGE_RESOURCE_ID_TAG = "message_resource_id"
        private const val MESSAGE_TAG = "message"
        private const val CANCELLABLE_TAG = "cancellable"

        @JvmStatic
        fun newInstance(titleResourceId: Int): GenericProgressDialogFragment {
            return newInstance(titleResourceId, true)
        }

        @JvmStatic
        fun newInstance(titleResourceId: Int, cancellable: Boolean): GenericProgressDialogFragment {
            val fragment = GenericProgressDialogFragment()
            val args = Bundle()
            args.putInt(MESSAGE_RESOURCE_ID_TAG, titleResourceId)
            args.putBoolean(CANCELLABLE_TAG, cancellable)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(message: String?): GenericProgressDialogFragment {
            return newInstance(message, true)
        }

        @JvmStatic
        fun newInstance(message: String?, cancellable: Boolean): GenericProgressDialogFragment {
            val fragment = GenericProgressDialogFragment()
            val args = Bundle()
            args.putString(MESSAGE_TAG, message)
            args.putBoolean(CANCELLABLE_TAG, cancellable)
            fragment.arguments = args
            return fragment
        }
    }
}
