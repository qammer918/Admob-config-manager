package com.mobile.test.application.domain.usecase

import com.mobile.test.application.domain.model.User
import com.mobile.test.application.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(query: String): Flow<List<User>> {
        return userRepository.searchUsers(query)
    }
}
