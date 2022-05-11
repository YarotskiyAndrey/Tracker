package com.training.tracker.ui

import com.training.tracker.model.User

data class MapViewState(val isSignedIn: Boolean, val userList: List<User>) {

    private constructor() : this(false, emptyList())

    companion object {
        val EmptyState = MapViewState()
    }
}