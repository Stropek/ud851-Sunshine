package com.example.android.sunshine

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.TextView

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

    companion object {

        private val FORECAST_SHARE_HASHTAG = " #SunshineApp"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        var menuItem = menu?.findItem(R.id.action_share)
        menuItem?.intent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecast + FORECAST_SHARE_HASHTAG)
                .intent
        return true
    }
}