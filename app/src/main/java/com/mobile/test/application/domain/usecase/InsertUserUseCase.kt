package com.mobile.test.application.domain.usecase

import com.mobile.test.application.domain.model.User
import com.mobile.test.application.domain.repository.UserRepository
import javax.inject.Inject

class InsertUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Long {
        return userRepository.insertUser(user)
    }
}
