package pl.latusikl.trackme.util

import android.content.Context
import pl.latusikl.trackme.TrackMeApp
import pl.latusikl.trackme.models.AppData
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object FileStore {

    private const val fileName = "trackme-data"


    fun writeToFile(appData: AppData) {

        TrackMeApp.applicationContext().openFileOutput(fileName, Context.MODE_PRIVATE)
            .use { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).writeObject(appData)
            }
    }


    fun readFromFile(): AppData {
        return TrackMeApp.applicationContext().openFileInput(fileName)
            .use { fileInputStream -> ObjectInputStream(fileInputStream).readObject() as AppData }
    }

    fun isDataFileCreated() : Boolean{
        return File(TrackMeApp.applicationContext().filesDir, fileName).exists()
    }
}
