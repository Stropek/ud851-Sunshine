/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.sync

import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.example.android.sunshine.data.WeatherContract


object SunshineSyncUtils {
    var sInitialized = false

    fun initialize(context: Context) {
        if (sInitialized)
            return
        sInitialized = true

        object: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {

                val cursor = context.contentResolver.query(WeatherContract.WeatherEntry.CONTENT_URI,
                        arrayOf(WeatherContract.WeatherEntry._ID),
                        WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards(),
                        null,
                        null)

                if (cursor == null || cursor.count == 0) {
                    startImmediateSync(context)
                }

                return null
            }
        }.execute()
    }

    /**
     * Helper method to perform a sync immediately using an IntentService for asynchronous
     * execution.
     *
     * @param context The Context used to start the IntentService for the sync.
     */
    fun startImmediateSync(context: Context) {
        val intentToSyncImmediately = Intent(context, SunshineSyncIntentService::class.java)
        context.startService(intentToSyncImmediately)
    }
}