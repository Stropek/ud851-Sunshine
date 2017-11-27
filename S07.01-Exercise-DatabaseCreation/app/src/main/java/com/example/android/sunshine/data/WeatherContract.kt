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
package com.example.android.sunshine.data

import android.provider.BaseColumns

open class KBaseColumns {
    val _ID = "_id"
    val _COUNT = "_count"
}

/**
 * Defines table and column names for the weather database. This class is not necessary, but keeps
 * the code organized.
 */
class WeatherContract {
    class WeatherEntry : BaseColumns {
        companion object : KBaseColumns() {
            val TABLE_NAME = "weather"
            val COLUMN_DATE = "date"
            val COLUMN_WEATHER_ID = "weather_id"
            val COLUMN_MIN_TEMP = "min"
            val COLUMN_MAX_TEMP = "max"
            val COLUMN_HUMIDITY = "humidity"
            val COLUMN_PRESSURE = "pressure"
            val COLUMN_WIND_SPEED = "wind"
            val COLUMN_DEGREES = "degrees"
        }
    }
}
