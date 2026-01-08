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
import com.module.remoteconfig.utils.logD
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class RemoteConfigViewModel @Inject constructor(private var remoteConfigRepository: RemoteConfigRepository) :
    ViewModel() {



    fun fetchRemoteConfig(onResponse: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            when (remoteConfigRepository.getRemoteResponse()) {
                RemoteConfigSource.Remote -> {
                    //for events
                    logD("RemoteConfigSource-->>", "Using latest remote config")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }

                RemoteConfigSource.Cache -> {
                    logD("RemoteConfigSource-->>", "Using cached config")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }

                RemoteConfigSource.Default -> {
                    logD("RemoteConfigSource-->>", "Using default IDs")
                    withContext(Dispatchers.Main) {
                        onResponse.invoke()
                    }
                }
            }


        }
    }


}