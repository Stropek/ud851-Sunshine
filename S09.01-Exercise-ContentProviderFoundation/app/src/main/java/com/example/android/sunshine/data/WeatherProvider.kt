/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.TargetApi
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri

/**
 * This class serves as the ContentProvider for all of Sunshine's data. This class allows us to
 * bulkInsert data, query data, and delete data.
 *
 *
 * Although ContentProvider implementation requires the implementation of additional methods to
 * perform single inserts, updates, and the ability to get the type of the data from a URI.
 * However, here, they are not implemented for the sake of brevity and simplicity. If you would
 * like, you may implement them on your own. However, we are not going to be teaching how to do
 * so in this course.
 */
class WeatherProvider : ContentProvider() {
    companion object {
        val CODE_WEATHER = 100
        val CODE_WEATHER_WITH_DATE = 101

        val sUriMatcher = buildUriMatcher()

        @JvmStatic
        fun buildUriMatcher(): UriMatcher {
            val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

            uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, CODE_WEATHER)
            uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, "${WeatherContract.PATH_WEATHER}/#", CODE_WEATHER_WITH_DATE)

            return uriMatcher
        }
    }

    private var mOpenHelper: WeatherDbHelper? = null

    override fun onCreate(): Boolean {
        mOpenHelper = WeatherDbHelper(context)
        return true
    }

    /**
     * Handles requests to insert a set of new rows. In Sunshine, we are only going to be
     * inserting multiple rows of data at a time from a weather forecast. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     * This must not be `null`.
     *
     * @return The number of values that were inserted.
     */
    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        throw RuntimeException("Student, you need to implement the bulkInsert mehtod!")
    }

    /**
     * Handles query requests from clients. We will use this method in Sunshine to query for all
     * of our weather data as well as to query for the weather on a particular day.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     * included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     * rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     * the values from selectionArgs, in order that they appear in the
     * selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        val db = mOpenHelper?.readableDatabase
        var cursor: Cursor? = null

        when (sUriMatcher.match(uri)) {
            CODE_WEATHER -> {
                cursor = db?.query(WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder)
            }
            CODE_WEATHER_WITH_DATE -> {
                val date = uri.lastPathSegment
                cursor = db?.query(WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        "${WeatherContract.WeatherEntry.COLUMN_DATE}=?",
                        Array<String>(1) { date },
                        null,
                        null,
                        sortOrder)
            }
            else -> UnsupportedOperationException("Unknown uri: $uri")
        }

        cursor?.setNotificationUri(context.contentResolver, uri)

        return cursor
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw RuntimeException("Student, you need to implement the delete method!")
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and getType is an abstract method in
     * ContentProvider. Normally, this method handles requests for the MIME type of the data at the
     * given URI. For example, if your app provided images at a particular URI, then you would
     * return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return nothing in Sunshine, but normally a MIME type string, or null if there is no type.
     */
    override fun getType(uri: Uri): String? {
        throw RuntimeException("We are not implementing getType in Sunshine.")
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and insert is an abstract method in
     * ContentProvider. Rather than the single insert method, we are only going to implement
     * [WeatherProvider.bulkInsert].
     *
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     * This must not be null
     * @return nothing in Sunshine, but normally the URI for the newly inserted item.
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw RuntimeException(
                "We are not implementing insert in Sunshine. Use bulkInsert instead")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw RuntimeException("We are not implementing update in Sunshine")
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @TargetApi(11)
    override fun shutdown() {
        mOpenHelper!!.close()
        super.shutdown()
    }
}