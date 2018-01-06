import android.content.Context
import com.example.android.sunshine.data.WeatherContract
import com.example.android.sunshine.utilities.NetworkUtils
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils

class SunshineSyncTask {
    companion object {

        @Synchronized
        fun syncWeather(context: Context) {
            val url = NetworkUtils.getUrl(context)
            val response = NetworkUtils.getResponseFromHttpUrl(url)
            val contentValues = OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context, response)

            if (contentValues != null && contentValues.isNotEmpty()) {
                context.contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, null, null)
                context.contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues)
            }
        }
    }
}
