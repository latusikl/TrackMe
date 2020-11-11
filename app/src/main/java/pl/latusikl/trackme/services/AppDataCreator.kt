package pl.latusikl.trackme.services

import pl.latusikl.trackme.models.AppData

object AppDataCreator {

    private const val EMPTY_STRING = ""

    fun createAppDataWithId(newUuid : String) : AppData {
        return AppData(newUuid, EMPTY_STRING, EMPTY_STRING )
    }

    fun createAppDataWithServerInfo(ip : String, port : String, appData: AppData) : AppData{
        return AppData(appData.deviceId,ip,port)
    }

}
