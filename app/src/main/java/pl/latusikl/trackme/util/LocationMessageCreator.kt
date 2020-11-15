package pl.latusikl.trackme.util

import android.location.Location
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sign

object LocationMessageCreator {

    fun createStartMessage(deviceId: String): String = "##,imei:${deviceId},A;"

    fun createMessage(deviceId: String, location: Location, date: Date): String {
        val latitude = location.latitude
        val longitude = location.longitude
        return "imei:$deviceId,tracker,${formattedDate(date)},,F,,A,${processLatitude(latitude)},${
            latitudeDirection(
                latitude
            )
        },${processLongitude(longitude)},${longitudeDirection(longitude)},0,0;"
    }

    fun createNoLocationMessage(deviceId: String, date: Date) =
        "imei:$deviceId,tracker,${formattedDate(date)},,L,,A,0,X,0,X,0,0;"

    private fun formattedDate(date: Date): String {
        return SimpleDateFormat("yyMMddHHmmss").format(date)
    }

    private fun latitudeDirection(latitude: Double): String {
        if (latitude.sign == 1.0) {
            return "N"
        } else if (latitude.sign == -1.0) {
            return "W"
        }
        throw IllegalStateException("Invalid latitude value")
    }

    private fun longitudeDirection(longitude: Double): String {
        if (longitude.sign == 1.0) {
            return "E"
        } else if (longitude.sign == -1.0) {
            return "W"
        }
        throw IllegalStateException("Invalid latitude value")
    }

    private fun processDegrees(coordinate: Double): Pair<Int, Double> {
        val degrees = coordinate.toInt()
        val decimalMinutes = (coordinate - degrees) * 60
        return Pair(degrees, decimalMinutes)
    }

    private fun processLatitude(coordinate: Double): String {
        val pair = processDegrees(coordinate)
        return "${String.format("%02d", pair.first)}${pair.second}"
    }

    private fun processLongitude(coordinate: Double): String {
        val pair = processDegrees(coordinate)
        return "${String.format("%03d", pair.first)}${pair.second}"
    }
}
