package com.mobile.test.application.domain.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val age: Int
)
