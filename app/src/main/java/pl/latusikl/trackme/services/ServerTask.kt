package pl.latusikl.trackme.services

import android.util.Log
import java.net.ConnectException


class ServerTask(val port: Int, val ipAddress: String, val deviceId: String) :
    Thread("ServerTask") {

    private var serverConnector: ServerConnector? = null

    @Volatile
    private var shouldThreadEnd = false

    @Volatile
    private var isNewData = false

    @Volatile
    private var messageToSend = ""

    private var isLoginMessageSendWithSuccess = false
    private var isConnectionException = false

    override fun run() {
        createServerConnectorIfNotExists()
        logDeviceIfNotLogged()
        while (!shouldThreadEnd) {
            if (isNewData) {
                Log.e("ServerThread", messageToSend)
                if (isConnected()) {
                    serverConnector?.sendMessages(messageToSend)
                }
                isNewData = false
            }
        }
    }

    private fun createServerConnectorIfNotExists() {
        if (serverConnector == null) {
            try {
                serverConnector = ServerConnector(port, ipAddress)
            } catch (exception: ConnectException) {
                isConnectionException = true
            }
        }
    }

    private fun logDeviceIfNotLogged() {
        if (isConnected() && !isLoginMessageSendWithSuccess) {
            serverConnector?.sendMessages(LocationMessageCreator.createStartMessage(deviceId))
            this.isLoginMessageSendWithSuccess = true
        }
    }

    fun sendData(message: String) {
        this.messageToSend = message;
        isNewData = true
    }

    fun end() {
        serverConnector?.close()
        this.shouldThreadEnd = true
    }

    fun isConnected(): Boolean {
        return !isConnectionException && serverConnector?.isConnected() ?: false
    }
}
