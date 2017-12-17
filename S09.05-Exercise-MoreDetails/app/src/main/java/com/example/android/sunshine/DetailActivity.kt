/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine

import android.content.Intent
import android.database.Cursor
import android.media.tv.TvContentRating
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.app.ShareCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView

import com.example.android.sunshine.data.WeatherContract
import com.example.android.sunshine.utilities.SunshineDateUtils
import com.example.android.sunshine.utilities.SunshineWeatherUtils


class DetailActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        if (data == null || !data.moveToFirst())
            return

        val dateLong = data.getLong(INDEX_DATE)
        val dateString = SunshineDateUtils.getFriendlyDateString(this, dateLong, true)
        mDateView?.text = dateString

        val descId = data.getInt(INDEX_WAETHER_ID)
        val descString = SunshineWeatherUtils.getStringForWeatherCondition(this, descId)
        mDescriptionView?.text = descString

        val high = data.getDouble(INDEX_TEMP_MAX)
        mHighView?.text = SunshineWeatherUtils.formatTemperature(this, high)

        val low = data.getDouble(INDEX_TEMP_MIN)
        mLowView?.text = SunshineWeatherUtils.formatTemperature(this, low)

        val humidity = data.getFloat(INDEX_HUMIDITY)
        val humidityString = getString(R.string.format_humidity, humidity)
        mHumidityView?.text = humidityString

        val windSpeed = data.getFloat(INDEX_WIND_SPEED)
        val windDirection = data.getFloat(INDEX_WIND_DIRECTION)
        val windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection)
        mWindView?.text = windString

        val pressure = data.getFloat(INDEX_PRESSURE)
        val pressureString = getString(R.string.format_pressure, pressure)
        mPressureView?.text = pressureString

        mForecastSummary = "$dateString - $descString - $high / $low"
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        when(id) {
            ID_DETAILS_LOADER -> {
                return CursorLoader(this, mUri, WEATHER_DETAILS_PROJECTION, null, null, null)
            }
            else -> throw UnsupportedOperationException("Unknown loader: $id")
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
    }

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private var mForecastSummary: String? = null
    private var mUri: Uri? = null

    var mDateView: TextView? = null
    var mDescriptionView: TextView? = null
    var mHighView: TextView? = null
    var mLowView: TextView? = null
    var mHumidityView: TextView? = null
    var mWindView: TextView? = null
    var mPressureView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mDateView = findViewById(R.id.tv_display_date) as TextView
        mDescriptionView = findViewById(R.id.tv_display_description) as TextView
        mHighView = findViewById(R.id.tv_display_heigh_temp) as TextView
        mLowView = findViewById(R.id.tv_display_low_temp) as TextView
        mHumidityView = findViewById(R.id.tv_display_humidity) as TextView
        mWindView = findViewById(R.id.tv_display_wind) as TextView
        mPressureView = findViewById(R.id.tv_display_pressure) as TextView

        mUri = intent.data
        if (mUri == null)
            throw NullPointerException("Intent's URI is null!")

        supportLoaderManager.initLoader(ID_DETAILS_LOADER, savedInstanceState, this)
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see .onPrepareOptionsMenu
     *
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu)
        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Get the ID of the clicked item */
        val id = item.itemId

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            val shareIntent = createShareForecastIntent()
            startActivity(shareIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private fun createShareForecastIntent(): Intent {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary!! + FORECAST_SHARE_HASHTAG)
                .intent
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        return shareIntent
    }

    companion object {
        val WEATHER_DETAILS_PROJECTION = arrayOf(
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
        )

        val INDEX_DATE = 0
        val INDEX_TEMP_MIN = 1
        val INDEX_TEMP_MAX = 2
        val INDEX_HUMIDITY = 3
        val INDEX_PRESSURE = 4
        val INDEX_WIND_SPEED = 5
        val INDEX_WIND_DIRECTION = 6
        val INDEX_WAETHER_ID = 7

        val ID_DETAILS_LOADER = 45

        /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
        private val FORECAST_SHARE_HASHTAG = " #SunshineApp"
    }
}