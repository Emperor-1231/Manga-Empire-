package eu.kanade.tachiyomi.data.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.system.notificationManager
import androidx.core.app.NotificationManagerCompat

/**
 * Class to manage the basic information of all the notifications used in the app.
 */
object Notifications {

    /**
     * Common notification channel and ids used anywhere.
     */
    const val CHANNEL_COMMON = "common_channel"
    const val ID_UPDATER = 1
    const val ID_DOWNLOAD_IMAGE = 2
    const val ID_INSTALL = 3
    const val CHANNEL_UPDATED = "updated_channel"
    const val ID_INSTALLED = -6

    /**
     * Notification channel and ids used by the library updater.
     */
    const val CHANNEL_LIBRARY = "library_channel"
    const val ID_LIBRARY_PROGRESS = -101
    const val ID_LIBRARY_ERROR = -102

    /**
     * Notification channel and ids used by the downloader.
     */
    const val CHANNEL_DOWNLOADER = "downloader_channel"
    const val ID_DOWNLOAD_CHAPTER = -201
    const val ID_DOWNLOAD_CHAPTER_ERROR = -202

    /**
     * Notification channel and ids used by the library updater.
     */
    const val CHANNEL_NEW_CHAPTERS = "new_chapters_channel"
    const val ID_NEW_CHAPTERS = -301
    const val GROUP_NEW_CHAPTERS = "eu.kanade.tachiyomi.NEW_CHAPTERS"

    /**
     * Notification channel and ids used by the library updater.
     */
    private const val GROUP_EXTENSION_UPDATES = "group_extension_updates"
    const val CHANNEL_UPDATES_TO_EXTS = "updates_ext_channel"
    const val ID_UPDATES_TO_EXTS = -401

    const val CHANNEL_EXT_PROGRESS = "ext_update_progress_channel"
    const val ID_EXTENSION_PROGRESS = -402
    const val CHANNEL_EXT_UPDATED = "ext_updated_channel"
    const val ID_UPDATED_EXTS = -403

    private const val GROUP_BACKUP_RESTORE = "group_backup_restore"
    const val CHANNEL_BACKUP_RESTORE_PROGRESS = "backup_restore_progress_channel"
    const val ID_RESTORE_PROGRESS = -501
    const val ID_RESTORE_COMPLETE = -502
    const val CHANNEL_BACKUP_RESTORE_COMPLETE = "backup_restore_complete_channel"
    const val ID_BACKUP_PROGRESS = -502
    const val ID_BACKUP_COMPLETE = -503

    /**
     * Notification channel used for crash log file sharing.
     */
    const val CHANNEL_CRASH_LOGS = "crash_logs_channel"
    const val ID_CRASH_LOGS = -601

    private val deprecatedChannels = listOf(
        "backup_restore_channel"
    )

    /**
     * Creates the notification channels introduced in Android Oreo.
     *
     * @param context The application context.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // Delete old notification channels
        deprecatedChannels.forEach(context.notificationManager::deleteNotificationChannel)

        listOf(
            NotificationChannelGroup(GROUP_BACKUP_RESTORE, context.getString(R.string.backup_and_restore)),
            NotificationChannelGroup(GROUP_EXTENSION_UPDATES, context.getString(R.string.extension_updates)),
        ).forEach(context.notificationManager::createNotificationChannelGroup)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_COMMON,
                context.getString(R.string.common),
                NotificationManager.IMPORTANCE_LOW
            ),
            NotificationChannel(
                CHANNEL_LIBRARY,
                context.getString(R.string.updating_library),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_DOWNLOADER,
                context.getString(R.string.downloads),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_UPDATES_TO_EXTS,
                context.getString(R.string.extension_updates_pending),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                group = GROUP_EXTENSION_UPDATES
            },
            NotificationChannel(
                CHANNEL_NEW_CHAPTERS,
                context.getString(R.string.new_chapters),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                CHANNEL_BACKUP_RESTORE_PROGRESS,
                context.getString(R.string.progress),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                group = GROUP_BACKUP_RESTORE
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_BACKUP_RESTORE_COMPLETE,
                context.getString(R.string.complete),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                group = GROUP_BACKUP_RESTORE
                setShowBadge(false)
                setSound(null, null)
            },
            NotificationChannel(
                CHANNEL_CRASH_LOGS,
                context.getString(R.string.crash_logs),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        context.notificationManager.createNotificationChannels(channels)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val newChannels = listOf(
                NotificationChannel(
                    CHANNEL_EXT_PROGRESS,
                    context.getString(R.string.updating_extensions),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    group = GROUP_EXTENSION_UPDATES
                    setShowBadge(false)
                    setSound(null, null)
                },
                NotificationChannel(
                    CHANNEL_EXT_UPDATED,
                    context.getString(R.string.extensions_updated),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    group = GROUP_EXTENSION_UPDATES
                },
                NotificationChannel(
                    CHANNEL_UPDATED,
                    context.getString(R.string.update_completed),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(false)
                }
            )
            context.notificationManager.createNotificationChannels(newChannels)
        }
    }

    fun isNotificationChannelEnabled(context: Context, channelId: String?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelId.isNullOrBlank()) {
                val manager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId)
                return channel.importance != NotificationManager.IMPORTANCE_NONE
            }
            false
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
