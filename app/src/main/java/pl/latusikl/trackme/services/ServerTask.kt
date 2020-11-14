package pl.latusikl.trackme.services

import android.util.Log
import java.net.ConnectException


class ServerTask(val port : Int, val ipAddress: String, val deviceId : String) : Thread("ServerTask") {
    @Volatile
    private var shouldEnd = false

    @Volatile
    private var newData = false

    @Volatile
    private var message = ""

    private var isLogged = false

    private var serverConnector : ServerConnector? = null

    private var isConnectionException = false


    override fun run() {
        createServerConnectorIfNotExists()
        logDeviceIfNotLogged()
        while (!shouldEnd) {
            if (newData) {
                Log.e("ServerThread", message)
                if(isConnected()){
                    serverConnector?.sendMessages(message)
                }
                newData = false
            }
        }
    }

    private fun createServerConnectorIfNotExists(){
        if(serverConnector == null){
            try{
                serverConnector = ServerConnector(port,ipAddress)
            }
           catch (exception : ConnectException){
               isConnectionException = true
           }
        }
    }

    private fun logDeviceIfNotLogged(){
        if(isConnected() && !isLogged){
            sendRegisterMessage()
            this.isLogged = true;
        }
    }

    private fun sendRegisterMessage(){
        serverConnector?.sendMessages(LocationMessageCreator.createStartMessage(deviceId))
        this.isLogged=true;
    }

    fun sendData(message: String) {
        this.message = message;
        newData = true
    }

    fun end() {
        serverConnector?.close()
        this.shouldEnd = true
    }

    fun isConnected() : Boolean{
        return !isConnectionException && serverConnector?.isConnected() ?: false
    }
}
