package com.example.android.sunshine.sync

import android.app.IntentService
import android.content.Intent

class SunshineSyncIntentService: IntentService(SunshineSyncIntentService::class.java.name) {
    override fun onHandleIntent(intent: Intent?) {
        SunshineSyncTask.syncWeather(this)
    }
}
