package com.training.tracker.ui

import com.google.android.gms.maps.model.LatLng
import com.training.tracker.model.User

class FakeMapUI(
    private val onCreateMarker: (user: User) -> Unit = {},
    private val onRemoveFromMap: (userId: Long) -> Unit = {},
    private val onMoveCameraToPoints: (latLngList: List<LatLng>) -> Unit = {},
    private val onMoveMarker: (user: User) -> Unit = {}
) : MapUI {

    override fun createMarker(user: User) {
        onCreateMarker(user)
    }

    override fun removeFromMap(userId: Long) {
        onRemoveFromMap(userId)
    }

    override fun moveCameraToPoints(latLngList: List<LatLng>) {
        onMoveCameraToPoints(latLngList)
    }

    override fun moveMarker(user: User) {
        onMoveMarker(user)
    }
}
