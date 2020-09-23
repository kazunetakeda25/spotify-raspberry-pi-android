package com.rpi.streaming.streamingplayer

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

internal class ClientWorker
    (private val client: Socket, private var messageString: String) : Runnable {

    override fun run() {
        var line: String
        var `in`: BufferedReader? = null
        var out: PrintWriter? = null
        try {
            `in` = BufferedReader(InputStreamReader(client.getInputStream()))
            out = PrintWriter(client.getOutputStream(), true)
        } catch (e: IOException) {
            println("in or out failed")
            System.exit(-1)
        }

        while (true) {
            try {
                line = `in`!!.readLine()
                out!!.println(line)
                messageString += line
            } catch (e: IOException) {
                println("Read failed")
                System.exit(-1)
            }
        }
    }
}