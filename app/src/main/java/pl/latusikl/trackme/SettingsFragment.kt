package pl.latusikl.trackme

import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Patterns
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.latusikl.trackme.services.AppDataCreator
import pl.latusikl.trackme.services.FileStore

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
            val interval: Int = extractIntervalFromString(getIntervalInput(view))

            val isPortValid = isPortInputValid(port)
            val isIpValid = isIpAddressValid(ipAddress)

            if (isPortValid && isIpValid) {
                AppDataCreator.createAppDataWithServerInfo(
                    ipAddress,
                    port,
                    interval,
                    FileStore.readFromFile()
                )
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

    private fun extractIntervalFromString(intervalInput: String): Int {
        return intervalInput.substring(0, intervalInput.length - INTERVAL_SUFFIX_LENGTH).toInt()
    }

    private fun isIpAddressValid(ipAddress: String): Boolean {
        return Patterns.IP_ADDRESS.matcher(ipAddress).matches()
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }
}
