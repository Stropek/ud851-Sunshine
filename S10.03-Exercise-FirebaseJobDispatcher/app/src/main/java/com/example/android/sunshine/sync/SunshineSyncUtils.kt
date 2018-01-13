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

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.text.format.Time

import com.example.android.sunshine.data.WeatherContract
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import java.util.concurrent.TimeUnit

object SunshineSyncUtils {

    val SUNSHINE_SYNC_INTERVAL_HOURS = 3L
    val SUNSHINE_SYNC_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(SUNSHINE_SYNC_INTERVAL_HOURS).toInt()
    val SYNC_FLEXTIME_SECONDS = SUNSHINE_SYNC_INTERVAL_SECONDS

    private var sInitialized: Boolean = false

    val SYNC_SUNSHINE_TAG = "sunshine-sync-tag"

    fun schedulePeriodicSync(context: Context) {
        val driver = GooglePlayDriver(context)
        val dispatcher = FirebaseJobDispatcher(driver)

        val job = dispatcher.newJobBuilder()
                .setService(SunshineFirebaseJobService::class.java)
                .setTag(SYNC_SUNSHINE_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(SUNSHINE_SYNC_INTERVAL_SECONDS, SUNSHINE_SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build()

        dispatcher.schedule(job)
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     * ContentResolver
     */
    @Synchronized
    fun initialize(context: Context) {

        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized) return

        sInitialized = true

        schedulePeriodicSync(context)

        /*
         * We need to check to see if our ContentProvider has data to display in our forecast
         * list. However, performing a query on the main thread is a bad idea as this may
         * cause our UI to lag. Therefore, we create a thread in which we will run the query
         * to check the contents of our ContentProvider.
         */
        val checkForEmpty = Thread(Runnable {
            /* URI for every row of weather data in our weather table*/
            val forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI

            /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
            val projectionColumns = arrayOf(WeatherContract.WeatherEntry._ID)
            val selectionStatement = WeatherContract.WeatherEntry
                    .getSqlSelectForTodayOnwards()

            /* Here, we perform the query to check to see if we have any weather data */
            val cursor = context.contentResolver.query(
                    forecastQueryUri,
                    projectionColumns,
                    selectionStatement, null, null)
            /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */
            if (null == cursor || cursor.count == 0) {
                startImmediateSync(context)
            }

            /* Make sure to close the Cursor to avoid memory leaks! */
            cursor!!.close()
        })

        /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start()
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