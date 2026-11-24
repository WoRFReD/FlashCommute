// Version 1.00 - FlashCommute
// Developed for FlashChip by WoRFReD
package com.flashchip.flashcommute

import android.app.Application
import com.flashchip.flashcommute.notification.NotificationHelper

class FlashCommuteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }
}
