package com.training.tracker.model

open class UserRemoteData {
    class UserListData(val userList: List<User>) : UserRemoteData()
    class UserLocationData(val userLocation: UserLocation) : UserRemoteData()
}

data class User(
    val id: Long,
    val name: String,
    val image: String,
    val latitude: Double,
    val longitude: Double
)

data class UserLocation(
    val userId: Long,
    val latitude: Double,
    val longitude: Double
)