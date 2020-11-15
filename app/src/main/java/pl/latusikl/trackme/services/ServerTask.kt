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

    private var isConnectionException = false

    override fun run() {
        createServerConnectorIfNotExists()
        sendLoggingMessage()
        while (!shouldThreadEnd) {
            if (isNewData) {
                Log.d("ServerThread", messageToSend)
                if (isConnected()) {
                    serverConnector?.sendMessages(messageToSend)
                } else {
                    tryToReconnect()
                    if (isConnected()) {
                        serverConnector?.sendMessages(messageToSend)
                    }
                }
                isNewData = false
            }
        }
    }

    private fun createServerConnectorIfNotExists() {
        if (serverConnector == null) {
            try {
                serverConnector = ServerConnector(port, ipAddress)
                isConnectionException = false
            } catch (exception: ConnectException) {
                isConnectionException = true
            }
        }
    }

    private fun tryToReconnect() {
        serverConnector?.close()
        serverConnector = null
        createServerConnectorIfNotExists()
        sendLoggingMessage()
    }

    private fun sendLoggingMessage() {
        if (isConnected()) {
            serverConnector?.sendMessages(LocationMessageCreator.createStartMessage(deviceId))
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
