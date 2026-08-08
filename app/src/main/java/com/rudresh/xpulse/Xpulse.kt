package com.rudresh.xpulse

import android.app.Application
import com.rudresh.xpulse.core.reminder.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Xpulse : Application() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        reminderScheduler.ensureChannel()
    }
}
