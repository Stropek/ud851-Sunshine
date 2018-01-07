package com.example.android.sunshine.utilities


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.example.android.sunshine.DetailActivity

import com.example.android.sunshine.R
import com.example.android.sunshine.data.SunshinePreferences
import com.example.android.sunshine.data.WeatherContract
import kotlin.String.Companion

object NotificationUtils {

    /*
     * The columns of data that we are interested in displaying within our notification to let
     * the user know there is new weather data available.
     */
    val WEATHER_NOTIFICATION_PROJECTION = arrayOf(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    val INDEX_WEATHER_ID = 0
    val INDEX_MAX_TEMP = 1
    val INDEX_MIN_TEMP = 2

    val SUNSHINE_NOTIFICATION_ID = 11

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param context Context used to query our ContentProvider and use various Utility methods
     */
    fun notifyUserOfNewWeather(context: Context) {

        /* Build the URI for today's weather in order to show up to date data in notification */
        val todaysWeatherUri = WeatherContract.WeatherEntry
                .buildWeatherUriWithDate(SunshineDateUtils.normalizeDate(System.currentTimeMillis()))

        /*
         * The MAIN_FORECAST_PROJECTION array passed in as the second parameter is defined in our WeatherContract
         * class and is used to limit the columns returned in our cursor.
         */
        val todayWeatherCursor = context.contentResolver.query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null, null, null)

        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, we want to show the notification.
         */
        if (todayWeatherCursor!!.moveToFirst()) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            val weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID)
            val high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP)
            val low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP)

            val resources = context.resources
            val largeArtResourceId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId)

            val largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId)

            val notificationTitle = context.getString(R.string.app_name)

            val notificationText = getNotificationText(context, weatherId, high, low)

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            val smallArtResourceId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId)

            val notification = NotificationCompat.Builder(context)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true)

            val intent = Intent(context, DetailActivity::class.java)
            intent.data = todaysWeatherUri

            val taskBuilder = TaskStackBuilder.create(context)
            taskBuilder.addNextIntentWithParentStack(intent)
            val pendingIntent = taskBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            notification.setContentIntent(pendingIntent)

            val notificationManger = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManger.notify(SUNSHINE_NOTIFICATION_ID, notification.build())
            SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis())
        }

        /* Always close your cursor when you're done with it to avoid wasting resources. */
        todayWeatherCursor.close()
    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     *
     *
     * The String returned from this method will look something like this:
     *
     *
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private fun getNotificationText(context: Context, weatherId: Int, high: Double, low: Double): String {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        val shortDescription = SunshineWeatherUtils
                .getStringForWeatherCondition(context, weatherId)

        val notificationFormat = context.getString(R.string.format_notification)

        /* Using String's format method, we create the forecast summary */

        return String.format(notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature(context, high),
                SunshineWeatherUtils.formatTemperature(context, low))
    }
}
