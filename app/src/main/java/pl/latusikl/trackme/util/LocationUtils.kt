/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.latusikl.trackme.util

import android.content.Context
import android.location.Location
import androidx.core.content.edit
import pl.latusikl.trackme.R


fun Location?.toText(): String {
    return if (this != null) {
        "$latitude, $longitude"
    } else {
        "Unknown location"
    }
}


internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"
    const val KEY_LAST_SEND_DATE = "tracking_foreground_timestamp"
    const val KEY_LAST_LOCATION = "tracking_last_location"


    fun saveLastLocationValue(context: Context, location: String) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).edit {
            putString(KEY_LAST_LOCATION, location)
        }

    fun getLastLocationValue(context: Context): String =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
            .getString(KEY_LAST_LOCATION, context.getString(R.string.location_unknown)).toString()


    fun saveLocationTimeStamp(context: Context, timeStamp: String) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).edit {
            putString(KEY_LAST_SEND_DATE, timeStamp)
        }

    fun getLastLocationTimeStamp(context: Context): String =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
            .getString(KEY_LAST_SEND_DATE, "").toString()


    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
            .getBoolean(KEY_FOREGROUND_ENABLED, false)


    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }
}
