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
import android.provider.Settings
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import com.example.android.sunshine.data.SunshinePreferences
import com.example.android.sunshine.utilities.NetworkUtils
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils

import java.net.URL

class MainActivity : AppCompatActivity(), ForecastAdapter.ForecastAdapterOnClickHandler, LoaderCallbacks<Array<String>> {

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
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         */
        val recyclerViewOrientation = LinearLayoutManager.VERTICAL

        /*
         *  This value should be true if you want to reverse your layout. Generally, this is only
         *  true with horizontal lists that need to support a right-to-left layout.
         */
        val shouldReverseLayout = false
        val layoutManager = LinearLayoutManager(this, recyclerViewOrientation, shouldReverseLayout)
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

        /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        val loaderId = FORECAST_LOADER_ID

        /*
         * From MainActivity, we have implemented the LoaderCallbacks interface with the type of
         * String array. (implements LoaderCallbacks<String[]>) The variable callback is passed
         * to the call to initLoader below. This means that whenever the loaderManager has
         * something to notify us of, it will do so through this callback.
         */
        val callback = this@MainActivity

        /*
         * The second parameter of the initLoader method below is a Bundle. Optionally, you can
         * pass a Bundle to initLoader that you can then access from within the onCreateLoader
         * callback. In our case, we don't actually use the Bundle, but it's here in case we wanted
         * to.
         */
        val bundleForLoader: Bundle? = null

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        supportLoaderManager.initLoader(loaderId, bundleForLoader, callback)

        Log.d(TAG, "onCreate: registering preference changed listener")
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param loaderArgs Any arguments supplied by the caller.
     *
     * @return Return a new Loader instance that is ready to start loading.
     */
    override fun onCreateLoader(id: Int, loaderArgs: Bundle?): Loader<Array<String>> {

        return object : AsyncTaskLoader<Array<String>>(this) {

            /* This String array will hold and help cache our weather data */
            internal var mWeatherData: Array<String>? = null

            /**
             * Subclasses of AsyncTaskLoader must implement this to take care of loading their data.
             */
            override fun onStartLoading() {
                if (mWeatherData != null) {
                    deliverResult(mWeatherData)
                } else {
                    mLoadingIndicator!!.visibility = View.VISIBLE
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
                        .getPreferredWeatherLocation(this@MainActivity)

                val weatherRequestUrl = NetworkUtils.buildUrl(locationQuery)

                try {
                    val jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl)

                    return OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(this@MainActivity, jsonWeatherResponse)
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

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    override fun onLoadFinished(loader: Loader<Array<String>>, data: Array<String>?) {
        mLoadingIndicator!!.visibility = View.INVISIBLE
        mForecastAdapter!!.setWeatherData(data)
        if (null == data) {
            showErrorMessage()
        } else {
            showWeatherDataView()
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    override fun onLoaderReset(loader: Loader<Array<String>>) {
        /*
         * We aren't using this method in our example application, but we are required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private fun invalidateData() {
        mForecastAdapter!!.setWeatherData(null)
    }

    /**
     * This method uses the URI scheme for showing a location found on a map in conjunction with
     * an implicit Intent. This super-handy intent is detailed in the "Common Intents" page of
     * Android's developer site:
     *
     * @see "http://developer.android.com/guide/components/intents-common.html.Maps"
     *
     *
     * Protip: Hold Command on Mac or Control on Windows and click that link to automagically
     * open the Common Intents page
     */
    private fun openLocationInMap() {
        val addressString = "1600 Ampitheatre Parkway, CA"
        val geoLocation = Uri.parse("geo:0,0?q=" + addressString)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geoLocation

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!")
        }
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param weatherForDay String describing weather details for a particular day
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

        if (id == R.id.action_settings) {
            val context = this
            val destinationClass = SettingsActivity::class.java
            var intent = Intent(context, destinationClass)

            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        private val TAG = MainActivity::class.java!!.getSimpleName()

        private val FORECAST_LOADER_ID = 0
    }
}