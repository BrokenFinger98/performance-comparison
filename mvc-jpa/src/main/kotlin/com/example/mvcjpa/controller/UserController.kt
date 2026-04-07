package com.example.mvcjpa.controller

import com.example.mvcjpa.domain.User
import com.example.mvcjpa.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun findAll(): List<User> = userService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): User = userService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody user: User): User = userService.create(user)

    @GetMapping("/aggregate")
    fun aggregate(): Map<String, Any> = userService.aggregate()
}
