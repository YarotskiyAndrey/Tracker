package com.training.tracker.model

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserParser {

    suspend fun parseData(
        message: String,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ): UserRemoteData {
        val userData = withContext(defaultDispatcher) {
            when {
                message.startsWith(UserRepository.MessageHead.USER_LIST.head) -> parseList(
                    message.drop(UserRepository.MessageHead.USER_LIST.head.length + 1)
                )
                message.startsWith(UserRepository.MessageHead.USER_UPDATE.head) -> parseUserLocation(
                    message.drop(UserRepository.MessageHead.USER_UPDATE.head.length + 1)
                )
                else -> throw IllegalStateException("Invalid server message")
            }
        }
        return userData
    }

    private fun parseList(data: String): UserRemoteData {
        val users = data.split(';')
            .dropLast(1)//last is always empty

        val userList = users.map { user ->
            val attributes = user.split(',')
            User(
                id = attributes[0].toLong(),
                name = attributes[1],
                image = attributes[2],
                latitude = attributes[3].toDouble(),
                longitude = attributes[4].toDouble()
            )
        }
        return UserRemoteData.UserListData(userList)
    }

    private fun parseUserLocation(data: String): UserRemoteData {
        val attributes = data.split(',')
        val userLocation =
            UserLocation(
                userId = attributes[0].toLong(),
                latitude = attributes[1].toDouble(),
                longitude = attributes[2].toDouble()
            )

        return UserRemoteData.UserLocationData(userLocation)
    }
}