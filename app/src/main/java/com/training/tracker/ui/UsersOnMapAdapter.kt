package com.training.tracker.ui

import com.google.android.gms.maps.model.*
import com.training.tracker.model.User

class UsersOnMapAdapter(private val mapUI: MapUI) {

    private val _userList = ArrayList<User>()

    fun updateUserList(userList: List<User>) {
        val userIdList = userList.map { it.id }
        val cachedUserIdList = _userList.map { it.id }

        val newUsers = userList.filterNot { user -> cachedUserIdList.contains(user.id) }
        val outDatedUserIds = cachedUserIdList.filterNot { id -> userIdList.contains(id) }
        val movedUsers = userList
            .filter { user -> cachedUserIdList.contains(user.id) }
            .filter { user ->
                val cachedUser = _userList.find { user.id == it.id }!!
                cachedUser.latitude != user.latitude || cachedUser.longitude != user.longitude
            }

        addUsers(newUsers)
        removeUsers(outDatedUserIds)
        updateLocation(movedUsers)
    }

    private fun addUsers(users: List<User>) {
        _userList.addAll(users)
        users.forEach(mapUI::createMarker)
        mapUI.moveCameraToPoints(users.map { LatLng(it.latitude, it.longitude) })
    }


    private fun removeUsers(userIdsToRemove: List<Long>) {
        _userList
            .filter { user -> userIdsToRemove.contains(user.id) }
            .forEach(_userList::remove)

        userIdsToRemove.forEach(mapUI::removeFromMap)
    }

    private fun updateLocation(userList: List<User>) {
        userList.forEach { user ->
            mapUI.moveMarker(user)

            val oldUser = _userList.find { it.id == user.id }
            _userList.remove(oldUser)
            _userList.add(user)
        }
    }
}

