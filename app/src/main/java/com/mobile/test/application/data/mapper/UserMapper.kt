package com.mobile.test.application.data.mapper

import com.mobile.test.application.data.local.entity.UserEntity
import com.mobile.test.application.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        age = age
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        age = age
    )
}


