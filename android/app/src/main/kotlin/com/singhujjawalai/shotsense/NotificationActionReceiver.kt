package com.singhujjawalai.shotsense

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Handles action buttons on screenshot alert notifications (e.g. "Trash now"
 * on an OTP screenshot). Actions carry the MediaStore uri and the original
 * path; the alert notification is dismissed after acting on it.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TRASH_NOW = "com.singhujjawalai.shotsense.TRASH_NOW"
        const val EXTRA_PATH = "path"
        const val EXTRA_URI = "uri"
        private const val TAG = "NotificationActions"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            ACTION_TRASH_NOW -> {
                val path = intent.getStringExtra(EXTRA_PATH) ?: return
                val trashed = DeletionManager.moveToTrash(context, path)
                Log.i(TAG, "Trash-now action for $path: $trashed")
                val uri = intent.getStringExtra(EXTRA_URI)
                if (uri != null) {
                    context.getSystemService(NotificationManager::class.java)
                        .cancel(uri.hashCode())
                }
            }
        }
    }
}
