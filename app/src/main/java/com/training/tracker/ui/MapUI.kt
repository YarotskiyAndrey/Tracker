package com.training.tracker.ui

import com.google.android.gms.maps.model.LatLng
import com.training.tracker.model.User

interface MapUI{
    fun createMarker(user: User)
    fun removeFromMap(userId: Long)
    fun moveCameraToPoints(latLngList: List<LatLng>)
    fun moveMarker(user: User)
}