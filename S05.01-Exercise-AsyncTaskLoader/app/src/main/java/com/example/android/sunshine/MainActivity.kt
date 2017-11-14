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
package com.example.android.sunshine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import com.example.android.sunshine.ForecastAdapter.ForecastAdapterOnClickHandler
import com.example.android.sunshine.data.SunshinePreferences
import com.example.android.sunshine.utilities.NetworkUtils
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils

class MainActivity : LoaderCallbacks<Array<String>>, AppCompatActivity(), ForecastAdapterOnClickHandler {
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Array<String>>
            = WeatherAsyncTaskLoader(this@MainActivity, mLoadingIndicator)

    override fun onLoaderReset(loader: Loader<Array<String>>?) {
    }

    override fun onLoadFinished(loader: Loader<Array<String>>?, data: Array<String>?) {
        mLoadingIndicator?.visibility = View.INVISIBLE
        mForecastAdapter?.setWeatherData(data)

        if (data == null) {
            showErrorMessage()
        }
        else {
            showWeatherDataView()
        }
    }

    private var mRecyclerView: RecyclerView? = null
    private var mForecastAdapter: ForecastAdapter? = null

    private var mErrorMessageDisplay: TextView? = null

    private var mLoadingIndicator: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = findViewById(R.id.recyclerview_forecast) as RecyclerView

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display) as TextView

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        mRecyclerView!!.layoutManager = layoutManager

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView!!.setHasFixedSize(true)

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = ForecastAdapter(this)

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView!!.adapter = mForecastAdapter

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator) as ProgressBar

        showWeatherDataView()

        val loaderId = FORECAST_LOADER_ID
        val callback: LoaderCallbacks<Array<String>> = this@MainActivity
        val bundle: Bundle? = null

        supportLoaderManager.initLoader(loaderId, bundle, callback)
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private fun loadWeatherData() {
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param weatherForDay The weather for the day that was clicked
     */
    override fun onClick(weatherForDay: String) {
        val context = this
        val destinationClass = DetailActivity::class.java
        val intentToStartDetailActivity = Intent(context, destinationClass)
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay)
        startActivity(intentToStartDetailActivity)
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private fun showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay!!.visibility = View.INVISIBLE
        /* Then, make sure the weather data is visible */
        mRecyclerView!!.visibility = View.VISIBLE
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private fun showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView!!.visibility = View.INVISIBLE
        /* Then, show the error */
        mErrorMessageDisplay!!.visibility = View.VISIBLE
    }

    /**
     * This method uses the URI scheme for showing a location found on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * @see <a></a>"http://developer.android.com/guide/components/intents-common.html.Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     */
    private fun openLocationInMap() {
        val addressString = "1600 Ampitheatre Parkway, CA"
        val geoLocation = Uri.parse("geo:0,0?q=" + addressString)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geoLocation

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu)
        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_refresh) {
            invalidateData()
            supportLoaderManager.restartLoader(FORECAST_LOADER_ID, null, this)
            return true
        }

        if (id == R.id.action_map) {
            openLocationInMap()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun invalidateData() {
        mForecastAdapter!!.setWeatherData(null)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val FORECAST_LOADER_ID = 0
    }

    class WeatherAsyncTaskLoader(context: Context, mLoadingIndicator: ProgressBar?): AsyncTaskLoader<Array<String>>(context) {
        /* This String array will hold and help cache our weather data */
        private var mWeatherData: Array<String>? = null
        private val loadingIndicator = mLoadingIndicator

        override fun onStartLoading() {
            if (mWeatherData != null) {
                deliverResult(mWeatherData)
            } else {
                loadingIndicator?.visibility = View.VISIBLE
                forceLoad()
            }
        }

        /**
         * This is the method of the AsyncTaskLoader that will load and parse the JSON data
         * from OpenWeatherMap in the background.
         *
         * @return Weather data from OpenWeatherMap as an array of Strings.
         * null if an error occurs
         */
        override fun loadInBackground(): Array<String>? {
            val locationQuery = SunshinePreferences
                    .getPreferredWeatherLocation(context)

            val weatherRequestUrl = NetworkUtils.buildUrl(locationQuery)

            try {
                val jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl)

                return OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(context, jsonWeatherResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * Sends the result of the load to the registered listener.
         *
         * @param data The result of the load
         */
        override fun deliverResult(data: Array<String>?) {
            mWeatherData = data
            super.deliverResult(data)
        }
    }
}