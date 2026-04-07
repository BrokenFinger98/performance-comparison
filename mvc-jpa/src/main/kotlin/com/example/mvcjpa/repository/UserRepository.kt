package com.example.mvcjpa.repository

import com.example.mvcjpa.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
