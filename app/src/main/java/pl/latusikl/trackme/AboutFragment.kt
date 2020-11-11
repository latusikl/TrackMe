package pl.latusikl.trackme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import pl.latusikl.trackme.models.AppData
import pl.latusikl.trackme.services.FileStore
import java.util.*

class AboutFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDeviceId(view)

    }

    private fun displayDeviceId(view: View){
        val deviceIdDisplay = if(FileStore.isDataFileCreated()){
            val appData = FileStore.readFromFile()
            appData.deviceId
        } else{
            val newDeviceId = UUID.randomUUID().toString()
            val appData = AppData(newDeviceId,"","")
            FileStore.writeToFile(appData)
            newDeviceId
        }
        view.findViewById<TextView>(R.id.aboutUuidTextView).text = deviceIdDisplay
    }

}
