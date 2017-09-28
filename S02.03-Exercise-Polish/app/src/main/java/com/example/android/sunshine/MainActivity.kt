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

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private var mWeatherTextView: TextView? = null

    private var mErrorMessageTextView: TextView? = null

    private var mLoadingProgressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mWeatherTextView = findViewById(R.id.tv_weather_data) as TextView

        mErrorMessageTextView = findViewById(R.id.tv_error_message) as TextView

        mLoadingProgressBar = findViewById(R.id.pb_loading) as ProgressBar

        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData()
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private fun loadWeatherData() {
        showWeatherDataView()
        val location = SunshinePreferences.getPreferredWeatherLocation(this)
        FetchWeatherTask().execute(location)
    }

    private fun showWeatherDataView() {
        mErrorMessageTextView?.visibility = View.INVISIBLE
        mWeatherTextView?.visibility = View.VISIBLE
    }

    private fun showErrorMessage() {
        mWeatherTextView?.visibility = View.INVISIBLE
        mErrorMessageTextView?.visibility = View.VISIBLE
    }

    inner class FetchWeatherTask : AsyncTask<String, Void, Array<String>>() {

        override fun onPreExecute() {
            mLoadingProgressBar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String): Array<String>? {

            /* If there's no zip code, there's nothing to look up. */
            if (params.size == 0) {
                return null
            }

            val location = params[0]
            val weatherRequestUrl = NetworkUtils.buildUrl(location)

            try {
                val jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl)

                val simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(this@MainActivity, jsonWeatherResponse)

                return OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(this@MainActivity, jsonWeatherResponse)

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

        }

        override fun onPostExecute(weatherData: Array<String>?) {
            mLoadingProgressBar?.visibility = View.INVISIBLE

            if (weatherData != null) {
                showWeatherDataView()
                /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
                for (weatherString in weatherData) {
                    mWeatherTextView!!.append(weatherString + "\n\n\n")
                }
            } else {
                showErrorMessage()
            }
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
            mWeatherTextView!!.text = ""
            loadWeatherData()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}