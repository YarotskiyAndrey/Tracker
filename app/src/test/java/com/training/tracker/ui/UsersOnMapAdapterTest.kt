package com.training.tracker.ui

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.training.tracker.model.User
import org.junit.Test

class UsersOnMapAdapterTest {
    @Test
    fun when_addNewUser_expect_createMarker() {
        val userList1 = listOf(
            User(1, "name", "image", 0.0, 0.0),
            User(2, "name", "image", 0.0, 0.0),
            User(3, "name", "image", 0.0, 0.0)
        )
        val userList2 = listOf(
            User(2, "name", "image", 0.0, 0.0),
            User(3, "name", "image", 0.0, 0.0),
            User(4, "name", "image", 0.0, 0.0)
        )

        val usersWithMarkers = ArrayList<User>()
        val mapUI = FakeMapUI(
            onCreateMarker = {
                usersWithMarkers.add(it)
            }
        )
        val usersOnMapAdapter = UsersOnMapAdapter(mapUI)
        usersOnMapAdapter.updateUserList(userList1)
        usersOnMapAdapter.updateUserList(userList2)


        val uniqueUserIds = ArrayList<Long>()
        userList1.forEach { uniqueUserIds.add(it.id) }
        userList2.forEach {
            if (!uniqueUserIds.contains(it.id))
                uniqueUserIds.add(it.id)
        }

        val actual = usersWithMarkers.map { it.id }
        assertThat(actual).containsExactlyElementsIn(uniqueUserIds)
    }

    @Test
    fun when_addNewUser_expect_cameraMovesToUsers() {
        val userList1 = listOf(
            User(1, "name", "image", 0.0, 0.0),
            User(2, "name", "image", 0.0, 0.0),
        )

        val userList2 = listOf(
            User(2, "name", "image", 0.0, 0.0),
            User(3, "name", "image", 0.0, 0.0)
        )

        val movedCameraPoints = ArrayList<List<LatLng>>()
        val mapUI = FakeMapUI(
            onMoveCameraToPoints = {
                movedCameraPoints.add(it)
            }
        )
        val usersOnMapAdapter = UsersOnMapAdapter(mapUI)
        usersOnMapAdapter.updateUserList(userList1)
        usersOnMapAdapter.updateUserList(userList2)

        val expected = ArrayList<List<LatLng>>()

        expected.add(userList1.map { LatLng(it.latitude, it.longitude) })

        expected.add(userList2
            .filter { user -> userList1.none { it.id == user.id } }
            .map { LatLng(it.latitude, it.longitude) })

        assertThat(movedCameraPoints).containsExactlyElementsIn(expected)
    }

    @Test
    fun when_addMovedUser_expect_moveMarker() {
        val userList1 = listOf(
            User(1, "name", "image", 0.0, 0.0),
            User(2, "name", "image", 0.0, 0.0),
        )

        val userList2 = listOf(
            User(1, "name", "image", 1.0, 1.0),
            User(2, "name", "image", 1.0, 1.0),
            User(3, "name", "image", 1.0, 1.0)
        )

        val movedUsers = ArrayList<User>()
        val mapUI = FakeMapUI(
            onMoveMarker = {
                movedUsers.add(it)
            }
        )

        val usersOnMapAdapter = UsersOnMapAdapter(mapUI)
        usersOnMapAdapter.updateUserList(userList1)
        usersOnMapAdapter.updateUserList(userList2)

        val expected = userList2.filter { user -> userList1.map { it.id }.contains(user.id) }

        assertThat(movedUsers).containsExactlyElementsIn(expected)
    }

    @Test
    fun when_removeUser_expect_removeMarker() {
        val userList1 = listOf(
            User(1, "name", "image", 1.0, 1.0),
            User(2, "name", "image", 1.0, 1.0),
            User(3, "name", "image", 1.0, 1.0)
        )
        val userList2 = emptyList<User>()

        val removedUserIds = ArrayList<Long>()
        val mapUI = FakeMapUI(
            onRemoveFromMap = {
                removedUserIds.add(it)
            }
        )
        val usersOnMapAdapter = UsersOnMapAdapter(mapUI)
        usersOnMapAdapter.updateUserList(userList1)
        usersOnMapAdapter.updateUserList(userList2)


        val expected = userList1.map { it.id }
        assertThat(removedUserIds).containsExactlyElementsIn(expected)
    }
}