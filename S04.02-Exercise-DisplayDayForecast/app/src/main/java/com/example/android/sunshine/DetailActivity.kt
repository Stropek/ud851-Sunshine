package com.example.android.sunshine

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        var intent = getIntent()

        var toast = Toast.makeText(this@DetailActivity, intent.getStringExtra("weatherData"), Toast.LENGTH_LONG)
        toast.show()
    }

    companion object {

        private val FORECAST_SHARE_HASHTAG = " #SunshineApp"
    }
}