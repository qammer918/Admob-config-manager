package com.module.remoteconfig.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.states.RemoteConfigSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class RemoteConfigViewModel @Inject constructor(private var remoteConfigRepository: RemoteConfigRepository) :
    ViewModel() {



    fun fetchRemoteConfig(onResponse: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            when (remoteConfigRepository.getRemoteResponse()) {
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