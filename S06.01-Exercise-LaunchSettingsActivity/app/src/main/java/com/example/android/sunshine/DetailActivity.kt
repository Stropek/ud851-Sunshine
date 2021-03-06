package com.example.android.sunshine

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.example.android.sunshine.DetailActivity.Companion.FORECAST_SHARE_HASHTAG

class DetailActivity : AppCompatActivity() {

    private var mForecast: String? = null
    private var mWeatherDisplay: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        mWeatherDisplay = findViewById(R.id.tv_display_weather) as TextView

        val intentThatStartedThisActivity = intent

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mForecast = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT)
                mWeatherDisplay!!.text = mForecast
            }
        }
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing. We set the
     * type of content that we are sharing (just regular text), the text itself, and we return the
     * newly created Intent.
     *
     * @return The Intent to use to start our share.
     */
    private fun createShareForecastIntent(): Intent {
        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast!! + FORECAST_SHARE_HASHTAG)
                .intent
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        val menuItem = menu.findItem(R.id.action_share)
        menuItem.intent = createShareForecastIntent()
        return true
    }

    companion object {

        private val FORECAST_SHARE_HASHTAG = " #SunshineApp"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val context = this
            val activity = SettingsActivity::class.java
            val intent = Intent(context, activity)

            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}