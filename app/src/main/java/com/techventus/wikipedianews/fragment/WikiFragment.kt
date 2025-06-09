package com.techventus.wikipedianews.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.dialogfragment.GenericProgressDialogFragment
import com.techventus.wikipedianews.dialogfragment.NonDismissableDialogFragment
import com.techventus.wikipedianews.dialogfragment.NotificationDialogFragment
import com.techventus.wikipedianews.logging.Logger
import org.apache.commons.lang3.StringUtils

/**
 * Created by josephmalone on 16-08-23.
 */
abstract class WikiFragment : Fragment() {

    private var mTitle: String? = null
    private var mToolbarCallback: ToolbarPropertyCallback? = null
    private var mErrorDialogFragment: DialogFragment? = null
    private var mForceUpgradeDialogFragment: NonDismissableDialogFragment? = null
    private var mIdentifier = 0
    private var mPayload: Bundle? = null
    private var mProgressDialog: GenericProgressDialogFragment? = null

    interface ToolbarPropertyCallback {
        fun setTitle(title: String?)
        fun setNavigationContentDescription(contentDescription: String?)
        fun setLogoDescription(description: String?)
        fun setSearchEnabled(enabled: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mIdentifier = savedInstanceState.getInt(DIALOG_IDENTIFIER_TAG)
            mPayload = savedInstanceState.getBundle(DIALOG_PAYLOAD_TAG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(DIALOG_IDENTIFIER_TAG, mIdentifier)
        if (mPayload != null) {
            outState.putBundle(DIALOG_PAYLOAD_TAG, mPayload)
        }
    }

    override fun onResume() {
        super.onResume()
        val fragmentManager = parentFragmentManager
        mErrorDialogFragment = fragmentManager.findFragmentByTag(ERROR_DIALOG_TAG) as DialogFragment?
        mErrorDialogFragment?.setTargetFragment(this, ERROR_DIALOG_REQUEST_CODE)
    }

    override fun onPause() {
        mErrorDialogFragment?.setTargetFragment(null, 0)
        mErrorDialogFragment = null
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ERROR_DIALOG_REQUEST_CODE && mErrorDialogFragment?.isAdded == true) {
            mErrorDialogFragment = null
            if (mIdentifier == POWER_SAVER_DIALOG_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                    } catch (e: Exception) {
                        // Handle the exception appropriately
                        Logger.e(TAG, "Failed to open Battery Saver settings", e)
                    }
                }
            } else {
                onErrorDialogDismissed(mIdentifier, mPayload, resultCode)
                mPayload = null
            }
        }
    }

    /**
     * Called when a dialog has been dismissed. To be overridden in child classes
     */
    protected open fun onErrorDialogDismissed(identifier: Int, data: Bundle?, buttonId: Int) {}

    open fun handleOnClickForNonDismissableDialogs(identifier: Int, buttonId: Int) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarPropertyCallback) {
            Logger.d(TAG, "$context implements toolbar callbacks. Registering")
            mToolbarCallback = context
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is ToolbarPropertyCallback) {
            Logger.d(TAG, "$activity implements toolbar callbacks. Registering")
            mToolbarCallback = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        mToolbarCallback = null
    }

    /**
     * Called by the activity to check whether the fragment wants to consume a back key press
     *
     * @return true if the event is consumed, false otherwise
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    /**
     * Shows a Snackbar with the app theme
     *
     * @param view       The view to find a parent from.
     * @param resourceId The resource id of the string resource to use. Can be formatted text.
     * @param length     How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
     */
    protected fun showSnackBar(view: View, @StringRes resourceId: Int, length: Int) {
        showSnackBar(view, resourceId, length, -1)
    }

    /**
     * Shows a Snackbar with the app theme
     *
     * @param view       The view to find a parent from.
     * @param resourceId The resource id of the string resource to use. Can be formatted text.
     * @param length     How long to display the message. Either LENGTH_SHORT or LENGTH_LONG
     * @param color      Background color for the Snackbar (resource ID)
     */
    protected fun showSnackBar(view: View, @StringRes resourceId: Int, length: Int, color: Int) {
        val snackbar = Snackbar.make(view, resourceId, length)
        if (color > 0) {
            val snackBarView = snackbar.view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                snackBarView.setBackgroundColor(resources.getColor(color, requireContext().theme))
            } else {
                snackBarView.setBackgroundColor(resources.getColor(color))
            }
        }
        snackbar.show()
    }

    /**
     * Call the activity callback that sets the content description. This Method takes an android
     * resource id and converts it to a string to call the callback method of the same name.
     *
     * @param contentDescriptionResourceId @NotNull The Resource ID of the string that we would like to set
     * as the content description.
     */
    fun setNavigationContentDescription(@StringRes contentDescriptionResourceId: Int) {
        mToolbarCallback?.setNavigationContentDescription(getString(contentDescriptionResourceId))
    }

    fun setSearchEnabled(enabled: Boolean) {
        mToolbarCallback?.setSearchEnabled(enabled)
    }

    /**
     * Call the activity callback that sets the logo description. This Method takes an android
     * resource id and converts it to a string to call the callback method of the same name.
     *
     * @param logoDescriptionResourceId @NotNull The Resource ID of the string that we would like to set
     * as the logo description.
     */
    fun setLogoDescription(@StringRes logoDescriptionResourceId: Int) {
        mToolbarCallback?.setLogoDescription(getString(logoDescriptionResourceId))
    }

    /**
     * Shows a network error dialog
     *
     * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     */
    protected fun showNoNetworkError(identifier: Int) {
        showNoNetworkError(identifier, true)
    }

    /**
     * Shows a network error dialog with only a retry button. Used in situations such as the onboarding screen where cancel does not make sense.
     *
     * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     */
    protected fun showNoNetworkError(identifier: Int, showCancel: Boolean) {
        if (showCancel) {
            showError(identifier, null, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection, R.string.cancel)
        } else {
            showError(identifier, R.string.error_title, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection)
        }
    }

    /**
     * Shows a network error dialog
     *
     * @param identifier an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     * @param data       a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
     */
    protected fun showNoNetworkError(identifier: Int, data: Bundle?) {
        showError(identifier, data, R.string.network_error_title, R.string.network_error_body, R.string.retry_connection, R.string.cancel)
    }

    /**
     * Shows a notification error dialog
     *
     * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     * @param titleResId          the resource ID to use for the dialog title
     * @param bodyResId           the resource ID to use for the dialog message
     * @param positiveButtonResId the resource ID to use for the positive button
     */
    protected fun showError(identifier: Int, @StringRes titleResId: Int, @StringRes bodyResId: Int, @StringRes positiveButtonResId: Int) {
        showError(identifier, null, titleResId, bodyResId, positiveButtonResId, -1)
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
    fun showError(identifier: Int, @StringRes titleResId: Int, @StringRes bodyResId: Int, @StringRes positiveButtonResId: Int, @StringRes negativeButtonResId: Int) {
        showError(identifier, null, titleResId, bodyResId, positiveButtonResId, negativeButtonResId)
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
    protected fun showError(identifier: Int, data: Bundle?, @StringRes titleResId: Int, @StringRes bodyResId: Int, @StringRes positiveButtonResId: Int, @StringRes negativeButtonResId: Int) {
        genericError(identifier, data, null, titleResId, null, bodyResId, positiveButtonResId, negativeButtonResId)
    }

    /**
     * Shows a notification error dialog
     *
     * @param identifier          an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     * @param data                a bundle that will be returned when the dialog is dismissed via onErrorDialogDismissed
     * @param titleString         the dialog title string
     * @param bodyString          the dialog body string
     * @param positiveButtonResId the resource ID to use for the positive button
     * @param negativeButtonResId the resource ID to use for the negative button
     */
    protected fun showError(identifier: Int, data: Bundle?, titleString: String?, bodyString: String?, @StringRes positiveButtonResId: Int, @StringRes negativeButtonResId: Int) {
        genericError(identifier, data, titleString, -1, bodyString, -1, positiveButtonResId, negativeButtonResId)
    }

    /**
     * Generic error handler to show dialogs
     */
    private fun genericError(identifier: Int, data: Bundle?, titleString: String?, @StringRes titleResId: Int, bodyString: String?, @StringRes bodyResId: Int, @StringRes positiveButtonResId: Int,
                             @StringRes negativeButtonResId: Int) {
        mIdentifier = identifier
        mPayload = data
        if (mErrorDialogFragment == null) {
            val dialogFragment: NotificationDialogFragment =
                if ((StringUtils.isNotEmpty(titleString) && titleResId == -1) || (StringUtils.isNotEmpty(bodyString) && bodyResId == -1)) {
                    NotificationDialogFragment.newInstance(
                        NotificationDialogFragment.ICON_ALERT,
                        titleString,
                        bodyString,
                        getString(positiveButtonResId),
                        if (negativeButtonResId > 0) getString(negativeButtonResId) else null
                    )
                } else {
                    NotificationDialogFragment.newInstance(
                        NotificationDialogFragment.ICON_ALERT,
                        titleResId,
                        bodyResId,
                        positiveButtonResId,
                        negativeButtonResId
                    )
                }
            // Ensure FragmentManager is not null
            val fragmentManager = parentFragmentManager
            mErrorDialogFragment = dialogFragment
            mErrorDialogFragment?.setTargetFragment(this, ERROR_DIALOG_REQUEST_CODE)
            mErrorDialogFragment?.show(fragmentManager, ERROR_DIALOG_TAG)
        } else {
//			if (BuildConfig.DEBUG) {
//				// Optionally throw exception or handle gracefully
//				// throw new IllegalStateException("Attempting to display an error dialog when one already exists");
//			} else {
//				Logger.e(TAG, "Attempting to display an error dialog when one already exists", new Exception());
//			}
        }
    }

    /**
     * Shows a force upgrade error dialog
     *
     * @param identifier     an integer that will be returned when the dialog is dismissed via onErrorDialogDismissed
     * @param titleString    the dialog title string
     * @param bodyString     the dialog body string
     * @param positiveButton the string to use for the positive button
     * @param negativeButton the string to use for the negative button
     */
    fun showForceUpgradeError(identifier: Int, titleString: String?, bodyString: String?, positiveButton: String?, negativeButton: String?) {
        if (mForceUpgradeDialogFragment == null) {
            mForceUpgradeDialogFragment = NonDismissableDialogFragment.newInstance(titleString, bodyString, positiveButton, negativeButton)
            mForceUpgradeDialogFragment?.setTargetFragment(this, identifier)
            mForceUpgradeDialogFragment?.show(parentFragmentManager, FORCE_UPGRADE_ERROR_DIALOG_TAG)
            mForceUpgradeDialogFragment?.isCancelable = false
        } else {
//			if (BuildConfig.DEBUG) {
//				throw new IllegalStateException("Attempting to display an error dialog when one already exists", new Exception());
//			} else {
//				Logger.e(TAG, "Attempting to display an error dialog when one already exists", new Exception());
//			}
        }
    }

    fun showProgress(@StringRes messageResId: Int, cancelable: Boolean) {
        dismissProgress()
        val fragmentManager = parentFragmentManager
        //TODO: fragmentManager can be null here - this was not handled in original Java code
//        if (fragmentManager != null) {
            mProgressDialog = GenericProgressDialogFragment.newInstance(messageResId, cancelable)
            if (mProgressDialog != null) {
                mProgressDialog?.show(fragmentManager, PROGRESS_DIALOG_TAG)
            } else {
//				if (BuildConfig.DEBUG) {
//					throw new IllegalStateException("Attempted to show progress dialog with null Fragment Manager, but dialog was null", new Exception());
//				} else {
//					Logger.e(TAG, "Attempted to show progress dialog with null Fragment Manager, but dialog was null", new Exception());
//				}
            }
//        } else {
//			if (BuildConfig.DEBUG) {
//				throw new IllegalStateException("Attempted to show progress dialog with null Fragment Manager", new Exception());
//			}
//			else {
//				Logger.e(TAG, "Attempted to show progress dialog with null Fragment Manager", new Exception());
//			}
//        }
    }

    /**
     * Dismisses the progress dialog. Safe to call if no progress dialog is being displayed.
     */
    fun dismissProgress() {
        if (mProgressDialog?.isAdded == true && parentFragmentManager != null) {
            mProgressDialog?.dismissAllowingStateLoss()
            mProgressDialog = null
        }
    }

    companion object {
        private val TAG = WikiFragment::class.java.simpleName
        private const val ERROR_DIALOG_TAG = "wiki_frag_error_dialog"
        private const val DIALOG_PAYLOAD_TAG = "payload"
        private const val DIALOG_IDENTIFIER_TAG = "dialog_identifier"
        private const val PROGRESS_DIALOG_TAG = "wiki_frag_progress_dialog"
        private const val FORCE_UPGRADE_ERROR_DIALOG_TAG = "force_upgrade_error_dialog"
        private const val ERROR_DIALOG_REQUEST_CODE = 54321
        private const val POWER_SAVER_DIALOG_CODE = 1234
        const val ERROR = "ERROR"
        private const val GENERIC_ERROR = "GENERIC_ERROR"
    }
}