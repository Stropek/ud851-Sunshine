package com.example.android.sunshine.sync

import android.content.Context
import android.content.Intent

object SunshineSyncUtils {
    @JvmStatic
    fun startImmediateSync(context: Context) {
        val intent = Intent(context, SunshineSyncIntentService::class.java)
        context.startService(intent)
    }
}
