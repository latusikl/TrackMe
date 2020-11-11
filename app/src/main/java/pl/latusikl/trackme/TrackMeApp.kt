package pl.latusikl.trackme

import android.app.Application
import android.content.Context


class TrackMeApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: TrackMeApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}
