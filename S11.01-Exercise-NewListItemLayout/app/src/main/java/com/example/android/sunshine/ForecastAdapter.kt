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
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.example.android.sunshine.utilities.SunshineDateUtils
import com.example.android.sunshine.utilities.SunshineWeatherUtils
import org.w3c.dom.Text

/**
 * [ForecastAdapter] exposes a list of weather forecasts
 * from a [android.database.Cursor] to a [android.support.v7.widget.RecyclerView].
 *
 * @param context Used to talk to the UI and app resources
 * @param clickHandler The on-click handler for this adapter. This single handler is called
 * when an item is clicked.
 */
internal class ForecastAdapter(private val mContext: Context, private val mClickHandler: ForecastAdapterOnClickHandler)
    : RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {

    /*
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set in the constructor of the adapter by accessing
     * boolean resources.
     */

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

        val view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.forecast_list_item, viewGroup, false)

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

        /*******************
         * Weather Summary *
         */
        /* Read date from the cursor */
        val dateInMillis = mCursor!!.getLong(MainActivity.INDEX_WEATHER_DATE)
        /* Get human readable string using our utility method */
        val dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false)
        /* Use the weatherId to obtain the proper description */
        val weatherId = mCursor!!.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID)
        val description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId)
        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = mCursor!!.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP)
        /* Read low temperature from the cursor (in degrees celsius) */
        val lowInCelsius = mCursor!!.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP)

        val weatherIconId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId)

        forecastAdapterViewHolder.mImageViewWeatherIcon.setImageDrawable(mContext.resources.getDrawable(weatherIconId))
        forecastAdapterViewHolder.mTextViewDay.text = dateString
        forecastAdapterViewHolder.mTextViewCondition.text = description
        forecastAdapterViewHolder.mTextViewMaxTemp.text = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius)
        forecastAdapterViewHolder.mTextViewMinTemp.text = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius)
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
    internal inner class ForecastAdapterViewHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        lateinit var mImageViewWeatherIcon: ImageView
        lateinit var mTextViewDay: TextView
        lateinit var mTextViewCondition: TextView
        lateinit var mTextViewMaxTemp: TextView
        lateinit var mTextViewMinTemp: TextView

        init {

            mImageViewWeatherIcon = view.findViewById(R.id.iv_weather_icon) as ImageView
            mTextViewDay = view.findViewById(R.id.tv_day) as TextView
            mTextViewCondition = view.findViewById(R.id.tv_condition) as TextView
            mTextViewMaxTemp = view.findViewById(R.id.tv_max_temp) as TextView
            mTextViewMinTemp = view.findViewById(R.id.tv_min_temp) as TextView

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