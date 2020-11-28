package pl.latusikl.trackme

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import pl.latusikl.trackme.services.LocationForegroundService
import pl.latusikl.trackme.util.ConnectionState
import pl.latusikl.trackme.util.FileStore
import pl.latusikl.trackme.util.SharedPreferenceUtil

class ConnectFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {


    private var locationForegroundServiceBound = false
    private var locationForegroundService: LocationForegroundService? = null
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var connectButton: Button
    private lateinit var locationInputTextView: TextView
    private lateinit var locationLastInputTextView: TextView
    private lateinit var connectionStatusTextView: TextView

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationForegroundService.LocalBinder
            locationForegroundService = binder.onlyLocationForegroundService
            locationForegroundServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationForegroundService = null
            locationForegroundServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.locationInputTextView = view.findViewById(R.id.current_location_input)
        this.connectButton = view.findViewById(R.id.connection_button)
        this.locationLastInputTextView = view.findViewById(R.id.last_send_input)
        this.connectionStatusTextView = view.findViewById(R.id.connection_status_input)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        sharedPreferences =
            requireActivity().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        clearUi()
        addButtonListener()
    }

    private fun addButtonListener() {
        connectButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

            if (FileStore.areSettingsReady()) {
                if (enabled) {
                    locationForegroundService?.unsubscribeToLocationUpdates()
                    clearUi()
                    //here add text change
                } else {
                    if (foregroundPermissionApproved()) {
                        clearUi()
                        locationInputTextView.text = getString(R.string.location_wait_info)
                        locationForegroundService?.subscribeToLocationUpdates()
                    } else {
                        requestForegroundPermissions()
                    }
                }
            } else {
                Toast.makeText(context, getText(R.string.set_server_settings), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun clearUi() {
        locationInputTextView.text = ""
        locationLastInputTextView.text = ""
        connectionStatusTextView.text = ""
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(activity, LocationForegroundService::class.java)
        activity?.bindService(
            serviceIntent,
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        updateUiValues()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                LocationForegroundService.ACTION_LOCATION_FETCHED
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (locationForegroundServiceBound) {
            activity?.unbindService(foregroundOnlyServiceConnection)
            locationForegroundServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED -> {
                updateButtonState(
                    SharedPreferenceUtil.getLocationWorkModePref(requireContext())
                )
            }
        }
    }


    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                   requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d("Connect Fragment", "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    locationForegroundService?.subscribeToLocationUpdates()

                else -> {
                    updateButtonState(false)

                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            connectButton.text = getString(R.string.stop_track_location_button_text)
        } else {
            connectButton.text = getString(R.string.start_track_location_button_text)
        }
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateUiValues()
        }
    }

    private fun updateUiValues() {
        val enabled = sharedPreferences.getBoolean(
            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
        )
        if (enabled) {
            locationInputTextView.text = SharedPreferenceUtil.getLastLocationValue(requireContext())
            locationLastInputTextView.text =
                SharedPreferenceUtil.getLastLocationTimeStamp(requireContext())
            connectionStatusTextView.text =
                parseStateToMessage(SharedPreferenceUtil.getServerStateValue(requireContext()))
        } else {
            clearUi()
        }
    }

    private fun parseStateToMessage(connectionState: String): String {
        return when (connectionState) {
            ConnectionState.CONNECTED.name -> getString(R.string.server_connection_ok)
            else -> {
                getString(R.string.server_connection_unable)
            }
        }
    }

    companion object {
        private const val FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

}

