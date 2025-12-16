package com.mobile.test.application.data.repository

import com.mobile.test.application.data.local.dao.UserDao
import com.mobile.test.application.data.mapper.toDomain
import com.mobile.test.application.data.mapper.toEntity
import com.mobile.test.application.domain.model.User
import com.mobile.test.application.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)?.toDomain()
    }
    
    override fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user.toEntity())
    }
    
    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }
    
    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }
    
    override suspend fun deleteUserById(id: Long) {
        userDao.deleteUserById(id)
    }
    
    override suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
}
