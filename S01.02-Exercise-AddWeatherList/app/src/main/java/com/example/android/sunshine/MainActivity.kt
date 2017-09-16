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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    var weatherDisplay : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        weatherDisplay = findViewById(R.id.tv_weather_data) as? TextView

        val weatherData = arrayOf("Sunny", "Cloudy", "Partially cloudy", "Super cloudy", "Hurricane",
                "Hurricane", "Fairly cloudy", "Sunny", "Sunny", "Humid")

        for(record in weatherData)
            weatherDisplay?.append(record + "\n\n\n")
    }
}