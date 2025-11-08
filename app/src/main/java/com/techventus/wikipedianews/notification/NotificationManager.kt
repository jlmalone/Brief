package com.techventus.wikipedianews.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for showing notifications in the app.
 *
 * Handles notification channels, permission checks, and notification creation.
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for Android O+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newsChannel = NotificationChannel(
                CHANNEL_NEWS_UPDATES,
                "News Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new news articles"
                enableVibration(true)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Background Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sync status notifications"
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(newsChannel)
            notificationManager.createNotificationChannel(syncChannel)

            Timber.d("Notification channels created")
        }
    }

    /**
     * Show notification for new news articles.
     *
     * @param articleCount Number of new articles
     */
    fun showNewsUpdateNotification(articleCount: Int) {
        if (!hasNotificationPermission()) {
            Timber.w("No notification permission, skipping notification")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEWS_UPDATES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Articles Available")
            .setContentText("$articleCount new article${if (articleCount > 1) "s" else ""} from Wikipedia Current Events")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NEWS_UPDATE, notification)
            Timber.i("Showed news update notification for $articleCount articles")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to show notification - permission denied")
        }
    }

    /**
     * Show notification for sync completion.
     */
    fun showSyncCompleteNotification() {
        if (!hasNotificationPermission()) {
            Timber.w("No notification permission, skipping sync notification")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Brief")
            .setContentText("News synced successfully")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SYNC, notification)
            Timber.d("Showed sync complete notification")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to show sync notification - permission denied")
        }
    }

    /**
     * Show notification for sync error.
     */
    fun showSyncErrorNotification(error: String) {
        if (!hasNotificationPermission()) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sync Failed")
            .setContentText(error)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SYNC_ERROR, notification)
            Timber.d("Showed sync error notification")
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to show error notification - permission denied")
        }
    }

    /**
     * Check if app has notification permission.
     * Always returns true on Android 12 and below.
     * On Android 13+, checks POST_NOTIFICATIONS permission.
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No permission needed on Android 12 and below
        }
    }

    companion object {
        private const val CHANNEL_NEWS_UPDATES = "news_updates"
        private const val CHANNEL_SYNC = "background_sync"

        private const val NOTIFICATION_ID_NEWS_UPDATE = 1
        private const val NOTIFICATION_ID_SYNC = 2
        private const val NOTIFICATION_ID_SYNC_ERROR = 3
    }
}
