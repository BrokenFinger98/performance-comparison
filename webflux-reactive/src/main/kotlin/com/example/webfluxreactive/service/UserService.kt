package com.example.webfluxreactive.service

import com.example.webfluxreactive.domain.User
import com.example.webfluxreactive.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    @Value("\${app.simulate-delay-ms:0}") private val simulateDelayMs: Long
) {

    fun findAll(): Flow<User> = userRepository.findAll().onStart {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs)  // non-blocking — releases thread
        }
    }

    suspend fun findById(id: Long): User {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs)
        }
        return userRepository.findById(id) ?: throw NoSuchElementException("User not found: $id")
    }

    suspend fun create(user: User): User {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs)
        }
        return userRepository.save(user)
    }
}
