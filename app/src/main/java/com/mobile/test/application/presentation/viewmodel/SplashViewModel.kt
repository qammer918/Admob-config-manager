package com.mobile.test.application.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.states.RemoteConfigSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private var remoteConfigRepository: RemoteConfigRepository) :
    ViewModel() {


    private val _remoteConfigSource =
        MutableStateFlow<RemoteConfigSource?>(null)

    val remoteConfigSource: StateFlow<RemoteConfigSource?> =
        _remoteConfigSource.asStateFlow()

    fun getRemoteResponse() = viewModelScope.launch(Dispatchers.IO) {
        remoteConfigRepository.getRemoteResponse()


    }

    fun fetchRemoteConfig() {
        viewModelScope.launch {
            val result = remoteConfigRepository.getRemoteResponsee()
            _remoteConfigSource.value = result
        }
    }


    fun fetchRemoteConfig(onResponse: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            remoteConfigRepository.getRemoteResponsee()

            withContext(Dispatchers.Main) {
                onResponse.invoke()
            }
        }
    }



//    fun fetchRemoteConfig(onResponse: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
//
//
//
//
//        when (remoteConfigRepository.getRemoteResponsee()) {
//            RemoteConfigSource.Remote -> {
//                Log.d("RC-->>", "Using latest remote config")
//                onResponse.invoke()
//            }
//            RemoteConfigSource.Cache -> {
//                Log.d("RC-->>", "Using cached config")
//                onResponse.invoke()
//            }
//            RemoteConfigSource.Default -> {
//                Log.d("RC-->>", "Using default IDs")
//                onResponse.invoke()
//            }
//        }
//    }

    fun setDefaultIds() {
        remoteConfigRepository.setDefaultIds()
    }

}