package com.mobile.test.application.domain.repository

import com.mobile.test.application.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(id: Long): User?
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun insertUser(user: User): Long
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun deleteUserById(id: Long)
    suspend fun deleteAllUsers()
}
