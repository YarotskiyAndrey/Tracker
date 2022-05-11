package com.training.tracker.model

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress

class TCPChannel constructor(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NetworkChannel {

    private lateinit var socket: Socket

    private lateinit var selectorManager: SelectorManager
    private lateinit var readChannel: ByteReadChannel
    private lateinit var writeChannel: ByteWriteChannel
    private var isChannelClosed = true

    override suspend fun openChannel() {
        selectorManager = ActorSelectorManager(ioDispatcher)
        withContext(ioDispatcher) {
            socket = aSocket(selectorManager).tcp()
                .connect(InetSocketAddress(HOST_NAME, PORT))
            readChannel = socket.openReadChannel()
            writeChannel = socket.openWriteChannel(autoFlush = true)
        }

        isChannelClosed = false
    }

    override suspend fun writeLine(message: String) {
        if (isChannelClosed)
            throw IllegalStateException(ACCESS_CLOSED_CHANNEL_ERROR)

        val validMessage =
            if (message.lastOrNull() == '\n') message
            else "$message\n"

        withContext(ioDispatcher) { writeChannel.writeStringUtf8(validMessage) }
    }

    override suspend fun readLine(): String {
        if (isChannelClosed)
            throw IllegalStateException(ACCESS_CLOSED_CHANNEL_ERROR)

        return withContext(ioDispatcher) { readChannel.readUTF8Line()!! }
    }

    override suspend fun closeChannel() {
        if (!isChannelClosed)
            withContext(ioDispatcher) {
                socket.close()
                selectorManager.close()
            }
    }

    companion object {
        private const val ACCESS_CLOSED_CHANNEL_ERROR = "Attempt to access closed channel"
        private const val HOST_NAME = "ios-test.printful.lv"
        private const val PORT = 6111
    }
}

interface NetworkChannel {
    suspend fun openChannel()
    suspend fun writeLine(message: String)
    suspend fun readLine(): String
    suspend fun closeChannel()
}
