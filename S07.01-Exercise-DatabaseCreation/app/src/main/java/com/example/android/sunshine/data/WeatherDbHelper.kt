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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Manages a local database for weather data.
 */
// TODO (11) Extend SQLiteOpenHelper from WeatherDbHelper
class WeatherDbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        val DATABASE_NAME = "weather.db"
        val DATABASE_VERSION = 1
    }

    override fun onCreate(mSQLiteDatabase: SQLiteDatabase) {
        val DATABASE_CREATE_SQL = "CREATE TABLE ${WeatherContract.WeatherEntry.TABLE_NAME} (" +
                "${WeatherContract.WeatherEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${WeatherContract.WeatherEntry.COLUMN_DATE} DATE NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_DEGREES} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_HUMIDITY} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_MAX_TEMP} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_MIN_TEMP} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_PRESSURE} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_WIND_SPEED} FLOAT NOT NULL," +
                "${WeatherContract.WeatherEntry.COLUMN_WEATHER_ID} INT NUT NULL" +
                ");"
        mSQLiteDatabase.execSQL(DATABASE_CREATE_SQL)
    }

    override fun onUpgrade(mSQLiteDatabase: SQLiteDatabase, previous: Int, next: Int) {

    }
}