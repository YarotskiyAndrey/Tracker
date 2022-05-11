package com.training.tracker.model

class NoInternetConnection : Exception() {
    override val message: String = "No Internet connection"
}

class SignOutException : Exception()
