package com.training.tracker.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.training.tracker.R
import com.training.tracker.databinding.WindowUserInfoBinding
import com.training.tracker.model.User

class UserMapUI(private val map: GoogleMap, private val context: Context) : MapUI {

    private val _usersOnMap = ArrayList<UserOnMap>()

    inner class UserInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(marker: Marker): View {
            val user = _usersOnMap.find { it.marker == marker }!!.user

            val userInfoViewBinding = WindowUserInfoBinding.inflate(LayoutInflater.from(context))

            userInfoViewBinding.tvUserName.text = user.name
            userInfoViewBinding.tvUserLocation.text =
                context.getString(R.string.marker_location, user.latitude, user.longitude)
            Glide.with(context)
                .load(user.image)
                .centerCrop()
                .into(userInfoViewBinding.ivUserImage)

            return userInfoViewBinding.root
        }

        override fun getInfoWindow(marker: Marker): View? = null
    }

    init {
        map.setInfoWindowAdapter(UserInfoWindowAdapter())
    }


    override fun createMarker(user: User) {
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_pokemon_marker)!!.toBitmap()
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(user.latitude, user.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )!!
        val userOnMap = UserOnMap(user, marker)

        _usersOnMap.add(userOnMap)
    }

    override fun removeFromMap(userId: Long) {
        val userOnMap = _usersOnMap.find { it.user.id == userId }!!
        userOnMap.marker.remove()
        _usersOnMap.remove(userOnMap)
    }


    override fun moveCameraToPoints(latLngList: List<LatLng>) {
        if (latLngList.isEmpty()) return
        val latLngBounds = latLngList.fold(LatLngBounds.builder()) { builder, latLng ->
            builder.include(latLng)
        }.build()

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, BOUNDS_PADDING))
    }

    private val _markerToPendingAnimation = HashMap<Marker, Animator?>()
    override fun moveMarker(user: User) {
        val userOnMap = _usersOnMap.find { it.user.id == user.id }!!
        val marker = userOnMap.marker
        val oldUser = userOnMap.user
        userOnMap.user = user

        val oldLocation = LatLng(oldUser.latitude, oldUser.longitude)
        val newLocation = LatLng(user.latitude, user.longitude)

        val animator = ValueAnimator.ofFloat(0f, 1f)
        _markerToPendingAnimation[marker]?.end()
        _markerToPendingAnimation[marker] = animator
        animator.duration = MARKER_ANIMATION_DURATION
        animator.addUpdateListener { updateAnimation ->
            val ratio = updateAnimation.animatedValue as Float

            val deltaLatitude = newLocation.latitude - oldLocation.latitude
            val deltaLongitude = newLocation.longitude - oldLocation.longitude

            val newLatitude = oldLocation.latitude + deltaLatitude * ratio
            val newLongitude = oldLocation.longitude + deltaLongitude * ratio

            marker.position = LatLng(newLatitude, newLongitude)
        }
        animator.doOnEnd {
            _markerToPendingAnimation.remove(marker)
        }
        animator.start()

        marker.position = newLocation
        if (marker.isInfoWindowShown)
            marker.showInfoWindow()//update info view
    }

    companion object {
        private const val MARKER_ANIMATION_DURATION = 750L
        private const val BOUNDS_PADDING = 400
    }

    class UserOnMap(var user: User, val marker: Marker)
}
