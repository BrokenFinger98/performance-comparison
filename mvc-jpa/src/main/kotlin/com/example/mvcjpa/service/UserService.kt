package com.example.mvcjpa.service

import com.example.mvcjpa.domain.User
import com.example.mvcjpa.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    @Value("\${app.simulate-delay-ms:0}") private val simulateDelayMs: Long
) {

    private fun simulateIoDelay() {
        if (simulateDelayMs > 0) {
            Thread.sleep(simulateDelayMs)  // blocking — occupies thread
        }
    }

    fun findAll(): List<User> {
        simulateIoDelay()
        return userRepository.findAll()
    }

    fun findById(id: Long): User {
        simulateIoDelay()
        return userRepository.findById(id).orElseThrow { NoSuchElementException("User not found: $id") }
    }

    fun create(user: User): User {
        simulateIoDelay()
        return userRepository.save(user)
    }

    fun count(): Long {
        simulateIoDelay()
        return userRepository.count()
    }

    fun findFirst(): User? {
        simulateIoDelay()
        return userRepository.findAll().firstOrNull()
    }

    // Sequential: 3 x 200ms = ~600ms
    fun aggregate(): Map<String, Any> {
        val users = findAll()
        val count = count()
        val first = findFirst()
        return mapOf(
            "users" to users,
            "count" to count,
            "first" to (first ?: "none")
        )
    }
}
