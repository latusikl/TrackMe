package pl.latusikl.trackme.services

import java.io.PrintWriter
import java.net.Socket

class ServerConnector(portNumber: Int, ipAddress: String) : AutoCloseable {

    private val socket: Socket = Socket(ipAddress, portNumber)
    private val output: PrintWriter

    init {
        output = PrintWriter(socket.getOutputStream(), true)
    }

    fun sendMessages(message: String) {
        output.println(message)
    }

    override fun close() {
        socket.close()
    }

    fun isConnected(): Boolean {
        return socket.isConnected && !output.checkError()
    }
}
