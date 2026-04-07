package com.example.webfluxreactive.repository

import com.example.webfluxreactive.domain.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository : CoroutineCrudRepository<User, Long>
