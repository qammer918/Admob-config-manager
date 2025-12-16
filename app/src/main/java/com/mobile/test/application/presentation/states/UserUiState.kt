package com.mobile.test.application.presentation.states

import com.mobile.test.application.domain.model.User

data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)