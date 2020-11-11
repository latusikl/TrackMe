package pl.latusikl.trackme.models

import java.io.Serializable

data class AppData(val deviceId : String, var ipAddress : String, var port: String, var sendingIntervalSeconds : Int) : Serializable {
}
