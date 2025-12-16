package com.mobile.test.application.domain.usecase

import com.mobile.test.application.domain.repository.UserRepository
import javax.inject.Inject

class DeleteUserByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Long) {
        userRepository.deleteUserById(id)
    }
}
