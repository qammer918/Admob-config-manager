package com.mobile.test.application.domain.usecase

import com.mobile.test.application.domain.model.User
import com.mobile.test.application.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        userRepository.updateUser(user)
    }
}
