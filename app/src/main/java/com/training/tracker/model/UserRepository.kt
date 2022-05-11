package com.training.tracker.model

import com.training.tracker.model.UserRepository.MessageHead.AUTHORIZE
import io.ktor.util.network.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*

class UserRepository(
    private val emailChannel: ReceiveChannel<String>,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private var _networkChannel: NetworkChannel? = null

    private val authorizationFLow = flow {
        while (true) {
            val email = emailChannel.receive()

            _networkChannel?.closeChannel()
            val channel: NetworkChannel = TCPChannel(ioDispatcher)
            _networkChannel = channel

            try {
                channel.openChannel()
                channel.writeLine("$AUTHORIZE $email")
                emit(Result.success(channel))
            } catch (e: UnresolvedAddressException) {
                emit(Result.failure(NoInternetConnection()))
            }
        }
    }.onCompletion {
        closeChannel()
    }

    val remoteDataFlow: Flow<Result<UserRemoteData>> =
        authorizationFLow.flatMapLatest { channelResult ->
            flow {
                if (channelResult.isSuccess)
                    try {
                        val channel = channelResult.getOrNull()!!
                        while (true) {
                            val message = channel.readLine()
                            val userRemoteData = UserParser.parseData(message, defaultDispatcher)
                            emit(Result.success(userRemoteData))
                        }
                    } catch (e: CancellationException) {
                        emit(Result.failure(SignOutException()))
                    }
                else
                    emit(Result.failure(channelResult.exceptionOrNull()!!))
            }
        }

    suspend fun closeChannel() {
        _networkChannel?.closeChannel()
        _networkChannel = null
    }

    enum class MessageHead(val head: String) {
        AUTHORIZE("AUTHORIZE"),
        USER_LIST("USERLIST"),
        USER_UPDATE("UPDATE");

        override fun toString() = head
    }
}

