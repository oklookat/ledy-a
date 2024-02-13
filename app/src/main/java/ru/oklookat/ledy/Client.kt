package ru.oklookat.ledy

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString


class Client(
    ctx: Context,
    private val onConnect: () -> Unit,
    private val onReconnect: () -> Unit
) : WebSocketListener() {

    private var conn: WebSocket? = null
    private var finder: ServerFinder = ServerFinder(ctx, {
        throw it
    }) { host, port ->
        val request = Request.Builder().url("ws://$host:$port").build()
        conn = OkHttpClient().newWebSocket(request, this)
    }

    init {
        Log.d("WEBSOCKET", "INIT")
        finder.find()
    }

    fun setColors(leds: List<RGB>) {
        if (conn == null) return
        val bytes = newCommandSetColors(leds).toByteString()
        conn?.send(bytes)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WEBSOCKET", "CONNECTED")
        onConnect()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WEBSOCKET", "CLOSE: $reason")
        reconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WEBSOCKET", "FAILURE: ${t.message.orEmpty()}")
        reconnect()
    }

    private fun reconnect() {
        onReconnect()
        conn?.cancel()
        finder.stop()
        finder.find()
    }
}