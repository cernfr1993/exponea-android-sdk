package com.exponea.sdk.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exponea.sdk.Exponea
import com.exponea.sdk.models.NotificationData
import com.exponea.sdk.util.Logger

class ExponeaPushReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CLICKED = "PushNotificationClicked"
        const val EXTRA_DATA = "NotificationData"

        fun getClickIntent(context: Context, data: NotificationData) : Intent {
            val intent = Intent(context, ExponeaPushReceiver::class.java)
            intent.apply {
                action = ACTION_CLICKED
                putExtra(EXTRA_DATA, data)
            }
            return intent
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CLICKED -> {
                Logger.i(this, "Push notification clicked")
                val data = intent.getParcelableExtra(EXTRA_DATA) as NotificationData
                Exponea.component.pushManager.trackClickedPush(
                        data = data
                )
            }
        }
    }
}