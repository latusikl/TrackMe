package pl.latusikl.trackme

import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.latusikl.trackme.services.AppDataCreator
import pl.latusikl.trackme.services.FileStore
import java.util.*

class SettingsFragment : Fragment() {

    private val MAX_PORT = 65535
    private val MIN_PORT = 1025
    private val INTERVAL_SUFFIX_LENGTH = 4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addApplyButtonClickListener(view)
        initiateSpinner(view)
        loadCurrentData(view)
    }

    private fun initiateSpinner(view: View) {
        view.findViewById<Spinner>(R.id.intervalSpinner).adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.intervalValues,
            android.R.layout.simple_spinner_dropdown_item
        )
    }

    private fun addApplyButtonClickListener(view: View) {
        view.findViewById<Button>(R.id.settingsApply).setOnClickListener {
            val ipAddress = getIpAddressInput(view)
            val port = getPortInput(view)
            val interval: Int = extractIntervalFromStringAsSeconds(getIntervalInput(view))

            val isPortValid = isPortInputValid(port)
            val isIpValid = isIpAddressValid(ipAddress)

            if (isPortValid && isIpValid) {
                val modifiedData = AppDataCreator.createAppDataWithServerInfo(
                    ipAddress,
                    port,
                    interval,
                    FileStore.readFromFile()
                )
                FileStore.writeToFile(modifiedData)
                showToast("Settings updated.")
            } else if (!isPortValid && !isIpValid) {
                showToast("IP address and port are invalid.")
            } else if (!isPortValid) {
                showToast("Port value is invalid.")
            } else {
                showToast("IP address is invalid.")
            }
        }
    }

    private fun loadCurrentData(view: View) {
        val currentAppData = FileStore.readFromFile()
        view.findViewById<EditText>(R.id.serverIpInput).setText(currentAppData.ipAddress)
        view.findViewById<EditText>(R.id.serverPortInput).setText(currentAppData.port)
        view.findViewById<Spinner>(R.id.intervalSpinner).setSelection(convertSecondsToPosition(currentAppData.sendingIntervalSeconds))
    }

    private fun convertSecondsToPosition(deviceIntervalSeconds : Int) : Int{
       return when(deviceIntervalSeconds){
            60 -> 0
            30 -> 1
            120 -> 2
            180 -> 3
            240 -> 4
            300 -> 5
            else -> 0
        }
    }

    private fun getIpAddressInput(view: View): String {
        return view.findViewById<EditText>(R.id.serverIpInput).text.toString()
    }


    private fun getPortInput(view: View): String {
        return view.findViewById<EditText>(R.id.serverPortInput).text.toString()
    }

    private fun getIntervalInput(view: View): String {
        return view.findViewById<Spinner>(R.id.intervalSpinner).selectedItem.toString()
    }

    private fun isPortInputValid(port: String): Boolean {
        return try {
            port.toInt() in (MIN_PORT..MAX_PORT)
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun extractIntervalFromStringAsSeconds(intervalInput: String): Int {
        println((intervalInput.substring(0, intervalInput.length - INTERVAL_SUFFIX_LENGTH).toDouble() * 60).toInt())
        return (intervalInput.substring(0, intervalInput.length - INTERVAL_SUFFIX_LENGTH).toDouble() * 60).toInt()
    }

    private fun isIpAddressValid(ipAddress: String): Boolean {
        return Patterns.IP_ADDRESS.matcher(ipAddress).matches()
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }
}
