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
import android.widget.ImageView
import android.widget.TextView

import com.example.android.sunshine.utilities.SunshineDateUtils
import com.example.android.sunshine.utilities.SunshineWeatherUtils

/**
 * [ForecastAdapter] exposes a list of weather forecasts
 * from a [android.database.Cursor] to a [android.support.v7.widget.RecyclerView].
 *
 * @param context      Used to talk to the UI and app resources
 * @param clickHandler The on-click handler for this adapter. This single handler is called
 * when an item is clicked.
 */
internal class ForecastAdapter(private val mContext: Context, private val mClickHandler: ForecastAdapterOnClickHandler)
    : RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {

    val VIEW_TODAY_ID = 1
    val VIEW_FUTURE_ID = 2

    /*
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set in the constructor of the adapter by accessing
     * boolean resources.
     */
    private var mUseTodayLayout = mContext.resources.getBoolean(R.bool.use_today_layout)
    private var mCursor: Cursor? = null

    /**
     * The interface that receives onClick messages.
     */
    interface ForecastAdapterOnClickHandler {
        fun onClick(date: Long)
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     * can use this viewType integer to provide a different layout. See
     * [android.support.v7.widget.RecyclerView.Adapter.getItemViewType]
     * for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ForecastAdapterViewHolder {

        var itemLayout = when(viewType) {
            VIEW_TODAY_ID -> R.layout.forecast_list_item_today
            VIEW_FUTURE_ID -> R.layout.forecast_list_item
            else -> throw IllegalArgumentException()
        }

        val view = LayoutInflater
                .from(mContext)
                .inflate(itemLayout, viewGroup, false)

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
        mCursor!!.moveToPosition(position)

        /****************
         * Weather Icon *
         */
        val weatherId = mCursor!!.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID)
        val weatherImageId: Int = when(forecastAdapterViewHolder.itemViewType) {
            VIEW_TODAY_ID -> SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)
            VIEW_FUTURE_ID -> SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId)
            else -> throw IllegalArgumentException()
        }

        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId)

        /****************
         * Weather Date *
         */
        /* Read date from the cursor */
        val dateInMillis = mCursor!!.getLong(MainActivity.INDEX_WEATHER_DATE)
        /* Get human readable string using our utility method */
        val dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false)

        /* Display friendly date string */
        forecastAdapterViewHolder.dateView.text = dateString

        /***********************
         * Weather Description *
         */
        val description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId)
        /* Create the accessibility (a11y) String from the weather description */
        val descriptionA11y = mContext.getString(R.string.a11y_forecast, description)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.text = description
        forecastAdapterViewHolder.descriptionView.contentDescription = descriptionA11y

        /**************************
         * High (max) temperature *
         */
        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = mCursor!!.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP)
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        val highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius)
        /* Create the accessibility (a11y) String from the weather description */
        val highA11y = mContext.getString(R.string.a11y_high_temp, highString)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.text = highString
        forecastAdapterViewHolder.highTempView.contentDescription = highA11y

        /*************************
         * Low (min) temperature *
         */
        /* Read low temperature from the cursor (in degrees celsius) */
        val lowInCelsius = mCursor!!.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP)
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        val lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius)
        val lowA11y = mContext.getString(R.string.a11y_low_temp, lowString)

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.lowTempView.text = lowString
        forecastAdapterViewHolder.lowTempView.contentDescription = lowA11y
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    override fun getItemCount(): Int {
        return if (null == mCursor) 0 else mCursor!!.count
    }

    override fun getItemViewType(position: Int): Int {
        if (mUseTodayLayout && position == 0)
            return VIEW_TODAY_ID

        return VIEW_FUTURE_ID
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    fun swapCursor(newCursor: Cursor) {
        mCursor = newCursor
        notifyDataSetChanged()
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    internal inner class ForecastAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val iconView: ImageView

        val dateView: TextView
        val descriptionView: TextView
        val highTempView: TextView
        val lowTempView: TextView

        init {

            iconView = view.findViewById(R.id.weather_icon) as ImageView
            dateView = view.findViewById(R.id.date) as TextView
            descriptionView = view.findViewById(R.id.weather_description) as TextView
            highTempView = view.findViewById(R.id.high_temperature) as TextView
            lowTempView = view.findViewById(R.id.low_temperature) as TextView

            view.setOnClickListener(this)
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        override fun onClick(v: View) {
            val adapterPosition = adapterPosition
            mCursor!!.moveToPosition(adapterPosition)
            val dateInMillis = mCursor!!.getLong(MainActivity.INDEX_WEATHER_DATE)
            mClickHandler.onClick(dateInMillis)
        }
    }
}