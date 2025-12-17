package com.mobile.test.application.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.states.RemoteConfigSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private var remoteConfigRepository: RemoteConfigRepository) :
    ViewModel() {


    /*    fun fetchRemoteConfig(onResponse: () -> Unit) {
            viewModelScope.launch(Dispatchers.IO) {
                remoteConfigRepository.getRemoteResponsee()

                withContext(Dispatchers.Main) {
                    onResponse.invoke()
                }
            }
        */



    fun fetchRemoteConfig(onResponse: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            when (remoteConfigRepository.getRemoteResponsee()) {
                RemoteConfigSource.Remote -> {
                    //for events
                    Log.d("RemoteConfigSource-->>", "Using latest remote config")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }

                RemoteConfigSource.Cache -> {
                    Log.d("RemoteConfigSource-->>", "Using cached config")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }

                RemoteConfigSource.Default -> {
                    Log.d("RemoteConfigSource-->>", "Using default IDs")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }
            }


        }
    }

}

