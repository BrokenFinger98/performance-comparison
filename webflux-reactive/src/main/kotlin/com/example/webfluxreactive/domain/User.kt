package com.example.webfluxreactive.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    val name: String = "",

    val email: String = "",

    val age: Int = 0
)
