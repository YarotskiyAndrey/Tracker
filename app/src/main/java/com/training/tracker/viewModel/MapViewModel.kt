package com.training.tracker.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.training.tracker.model.*
import com.training.tracker.model.UserRemoteData.UserListData
import com.training.tracker.model.UserRemoteData.UserLocationData
import com.training.tracker.ui.MapViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch

class MapViewModel(email: String?) : ViewModel() {

    private val emailChannel = Channel<String>(CONFLATED)
    private val repository = UserRepository(emailChannel)
    private val userListPersistentStorage = ArrayList<User>()
    private var emailCache: String? = email

    init {
        attemptToReLogin()
    }

    private val _stateFlow = repository.remoteDataFlow
        .map { result ->
            if (result.isFailure) {
                val exception = result.exceptionOrNull()!!
                if (exception !is SignOutException)
                    _errorSharedFlow.emit(exception.message.toString())
                MapViewState.EmptyState
            } else {
                updateCache(result.getOrNull()!!)
                MapViewState(isSignedIn = true, userList = userListPersistentStorage.toList())
            }
        }.onCompletion {
            attemptToReLogin()
        }.catch { exception ->
            emit(MapViewState.EmptyState)
            _errorSharedFlow.emit(exception.message.toString())
        }

    val stateFlow = _stateFlow
        .stateIn(viewModelScope, WhileSubscribed(FLOW_STOP_DELAY), MapViewState.EmptyState)

    private val _errorSharedFlow = MutableSharedFlow<String>()
    val errorSharedFlow = _errorSharedFlow.asSharedFlow()

    fun signIn(email: String) {
        viewModelScope.launch {
            emailCache = email
            emailChannel.send(email)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.closeChannel()
        }
    }

    private fun updateCache(data: UserRemoteData) {
        when (data) {
            is UserListData -> cacheUserList(data.userList)
            is UserLocationData -> modifyLocation(data.userLocation)
        }
    }

    private fun cacheUserList(userList: List<User>) {
        userListPersistentStorage.clear()
        userListPersistentStorage.addAll(userList)
    }

    private fun modifyLocation(userLocation: UserLocation) {
        val index = userListPersistentStorage.indexOfFirst { it.id == userLocation.userId }
        if (index != -1) {
            userListPersistentStorage[index] = userListPersistentStorage[index]
                .copy(
                    latitude = userLocation.latitude,
                    longitude = userLocation.longitude
                )
        }
    }

    private fun attemptToReLogin() {
        viewModelScope.launch {
            emailCache?.let { emailChannel.send(it) }
        }
    }

    companion object {
        private const val FLOW_STOP_DELAY = 5000L
    }
}

