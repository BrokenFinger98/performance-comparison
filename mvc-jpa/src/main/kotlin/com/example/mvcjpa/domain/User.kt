package com.example.mvcjpa.domain

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String = "",

    @Column(unique = true)
    val email: String = "",

    val age: Int = 0
)
