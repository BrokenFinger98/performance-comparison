package com.example.webfluxreactive.controller

import com.example.webfluxreactive.domain.User
import com.example.webfluxreactive.service.UserService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun findAll(): Flow<User> = userService.findAll()

    @GetMapping("/{id}")
    suspend fun findById(@PathVariable id: Long): User = userService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody user: User): User = userService.create(user)
}
