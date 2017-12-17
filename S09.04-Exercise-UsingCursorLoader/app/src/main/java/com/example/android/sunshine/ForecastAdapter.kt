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
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.android.sunshine.data.WeatherContract
import com.example.android.sunshine.utilities.SunshineDateUtils
import com.example.android.sunshine.utilities.SunshineWeatherUtils

/**
 * [ForecastAdapter] exposes a list of weather forecasts
 * from a [android.database.Cursor] to a [android.support.v7.widget.RecyclerView].
 */
/*
 * Below, we've defined an interface to handle clicks on items within this Adapter. In the
 * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
 * said interface. We store that instance in this variable to call the onClick method whenever
 * an item is clicked in the list.
 */
internal class ForecastAdapter(private val mClickHandler: ForecastAdapterOnClickHandler, private val mContext: Context)
    : RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {

    private var mCursor: Cursor? = null

    /**
     * The interface that receives onClick messages.
     */
    interface ForecastAdapterOnClickHandler {
        fun onClick(weatherForDay: String)
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     * can use this viewType integer to provide a different layout. See
     * [android.support.v7.widget.RecyclerView.Adapter.getItemViewType]
     * for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ForecastAdapterViewHolder {
        val context = viewGroup.context
        val layoutIdForListItem = R.layout.forecast_list_item
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false
        val view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately)
        return ForecastAdapterViewHolder(view)
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     * contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(forecastAdapterViewHolder: ForecastAdapterViewHolder, position: Int) {
        mCursor?.moveToPosition(position)

        val dateInMillis = mCursor!!.getLong(MainActivity.INDEX_WEATHER_DATE)
        val date = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, true)

        val weatherId = mCursor!!.getInt(MainActivity.INDEX_WEATHER_ID)
        val description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId)

        val highTemp = mCursor!!.getDouble(MainActivity.INDEX_MAX_TEMP)
        val lowTemp = mCursor!!.getDouble(MainActivity.INDEX_MIN_TEMP)
        val tempHighLow = SunshineWeatherUtils.formatHighLows(mContext, highTemp, lowTemp)

        val summary = "$date - $description - $tempHighLow"
        forecastAdapterViewHolder.weatherSummary.text = summary
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    override fun getItemCount(): Int {
        return if (mCursor == null) 0 else mCursor!!.count
    }

    fun swapCursor(cursor: Cursor?) {
        mCursor = cursor
        notifyDataSetChanged()
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    internal inner class ForecastAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val weatherSummary: TextView

        init {
            weatherSummary = view.findViewById(R.id.tv_weather_data) as TextView
            view.setOnClickListener(this)
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        override fun onClick(v: View) {
            val weatherForDay = weatherSummary.text.toString()
            mClickHandler.onClick(weatherForDay)
        }
    }
}