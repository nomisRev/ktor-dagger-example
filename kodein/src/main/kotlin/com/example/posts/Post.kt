package com.example.posts

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val createdAt: Long
)